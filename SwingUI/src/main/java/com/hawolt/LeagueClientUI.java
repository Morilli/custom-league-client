package com.hawolt;

import com.hawolt.async.ExecutorManager;
import com.hawolt.async.rms.GameStartListener;
import com.hawolt.client.IClientCallback;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.RiotClient;
import com.hawolt.client.misc.ClientConfiguration;
import com.hawolt.client.settings.client.ClientSettingsService;
import com.hawolt.client.settings.login.LoginSettings;
import com.hawolt.client.settings.login.LoginSettingsService;
import com.hawolt.logger.Logger;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.rtmp.amf.decoder.AMFDecoder;
import com.hawolt.shutdown.ShutdownHook;
import com.hawolt.ui.MainUI;
import com.hawolt.ui.chat.ChatSidebar;
import com.hawolt.ui.chat.friendlist.ChatSidebarFriendlist;
import com.hawolt.ui.chat.window.ChatUI;
import com.hawolt.ui.layout.LayoutManager;
import com.hawolt.ui.login.ILoginCallback;
import com.hawolt.ui.login.LoginUI;
import com.hawolt.ui.settings.SettingsUI;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.virtual.leagueclient.exception.LeagueException;
import com.hawolt.virtual.leagueclient.userinfo.UserInformation;
import com.hawolt.virtual.riotclient.instance.MultiFactorSupplier;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.EventListener;
import com.hawolt.xmpp.event.EventType;
import com.hawolt.xmpp.event.objects.other.PlainData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created: 11/08/2023 18:10
 * Author: Twitter @hawolt
 **/

public class LeagueClientUI extends JFrame implements IClientCallback, ILoginCallback, WindowStateListener {
    public static final ExecutorService service = ExecutorManager.registerService("pool", Executors.newCachedThreadPool());
    private LeagueClient leagueClient;
    private RiotClient riotClient;

    public LeagueClientUI(String title) {
        super(title);
        this.addWindowStateListener(this);
        this.addWindowListener(new WindowCloseHandler(this));
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private ChatSidebar chatSidebar;
    private LayoutManager manager;
    private ChatUI chatUI;
    private SettingsUI settingsUI;
    private LoginUI loginUI;
    private MainUI mainUI;

    @Override
    public void onClient(LeagueClient client) {
        buildUI(leagueClient = client);
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(client)));
        client.getRMSClient().getHandler().addMessageServiceListener(MessageService.GSM, new GameStartListener(this));
    }

    private void buildUI(LeagueClient client) {
        VirtualRiotXMPPClient xmppClient = client.getXMPPClient();
        mainUI = new MainUI(this);
        ChildUIComponent temporary = new ChildUIComponent(new BorderLayout());
        chatUI = new ChatUI();
        chatUI.setSupplier(xmppClient);
        chatUI.setVisible(false);
        mainUI.addChatComponent(chatUI);

        settingsUI = new SettingsUI();
        settingsUI.setVisible(false);
        mainUI.addSettingsComponent(settingsUI);

        UserInformation userInformation = client.getVirtualLeagueClient()
                .getVirtualLeagueClientInstance()
                .getUserInformation();
        chatSidebar = new ChatSidebar(userInformation, this);
        manager = new LayoutManager(this);
        temporary.add(manager, BorderLayout.CENTER);
        temporary.add(chatSidebar, BorderLayout.EAST);
        chatSidebar.configure(userInformation);
        if (leagueClient.getXMPP().getTimestamp() > 0) {
            buildSidebarUI(xmppClient, chatUI);
        } else {
            xmppClient.addHandler(
                    EventType.ON_READY,
                    (EventListener<PlainData>) event -> buildSidebarUI(xmppClient, chatUI)
            );
        }
        mainUI.setMainComponent(temporary);
        mainUI.revalidate();
    }

    private void buildSidebarUI(VirtualRiotXMPPClient xmppClient, ChatUI chatWindow) {
        chatSidebar.getProfile().getSummoner().getStatus().setXMPPClient(xmppClient);
        ChatSidebarFriendlist friendlist = chatSidebar.getChatSidebarFriendlist();
        friendlist.onEvent(xmppClient.getFriendList());
        xmppClient.addPresenceListener(friendlist);
        xmppClient.addFriendListener(friendlist);
        xmppClient.addMessageListener(chatWindow);
        friendlist.revalidate();
    }

    public LeagueClient getLeagueClient() {
        return leagueClient;
    }

    public LayoutManager getLayoutManager() {
        return manager;
    }

    public ChatSidebar getChatSidebar() {
        return chatSidebar;
    }

    public RiotClient getRiotClient() {
        return riotClient;
    }

    public LayoutManager getManager() {
        return manager;
    }

    public ChatUI getChatUI() {
        return chatUI;
    }

    public SettingsUI getSettingsUI() {
        return settingsUI;
    }

    public LoginUI getLoginUI() {
        return loginUI;
    }

    public MainUI getMainUI() {
        return mainUI;
    }

    private void showFailureDialog(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Login Failed",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public void onLoginFlowException(Throwable throwable) {
        Logger.error("Failed to initialize Client: {}", throwable.getMessage());
        if (throwable instanceof LeagueException e) {
            switch (e.getType()) {
                case NO_LEAGUE_ACCOUNT -> showFailureDialog("No League account connected");
                case NO_SUMMONER_NAME -> showFailureDialog("No name set for summoner");
            }
        } else if (throwable instanceof IOException) {
            switch (throwable.getMessage()) {
                case "AUTH_FAILURE" -> showFailureDialog("Invalid username or password");
                case "RATE_LIMITED" -> showFailureDialog("You are being rate limited");
            }
        } else {
            showFailureDialog("Unknown Error during login");
        }
        this.loginUI.toggle(true);
    }

    private ClientConfiguration getConfiguration(String username, String password) {
        JFrame parent = this;
        return ClientConfiguration.getDefault(username, password, new MultiFactorSupplier() {
            @Override
            public String get() {
                return (String) JOptionPane.showInputDialog(
                        parent,
                        "Enter 2FA Code",
                        "Multifactor",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "");
            }
        });
    }

    @Override
    public void onLogin(String username, String password) {
        this.createRiotClient(getConfiguration(username, password));
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        if (e.getNewState() == Frame.MAXIMIZED_BOTH) {
            mainUI.adjust();
        }
    }

    private void createRiotClient(ClientConfiguration configuration) {
        this.riotClient = new RiotClient(configuration, this);
    }

    public static void main(String[] args) {
        AMFDecoder.debug = false;
        LeagueClientUI leagueClientUI = new LeagueClientUI(StaticConstant.PROJECT);

        try {
            ClientSettingsService.get().readSettingsFile();
        } catch (IOException e1) {
            try {
                ClientSettingsService.get().writeSettingsFile();
            } catch (IOException e2) {
            }
        }

        try {
            LoginSettingsService.get().readSettingsFile();

            LoginSettings loginSettings = LoginSettingsService.get().getSettings();
            ClientConfiguration configuration = leagueClientUI.getConfiguration(
                    loginSettings.getUsername(),
                    loginSettings.getPassword()
            );
            leagueClientUI.createRiotClient(configuration);
        } catch (IOException e1) {
            try {
                LoginSettingsService.get().deleteSettingsFile();
            } catch (IOException e2) {
            }
            leagueClientUI.loginUI = LoginUI.show(leagueClientUI);
        }
        leagueClientUI.setVisible(true);
    }
}
