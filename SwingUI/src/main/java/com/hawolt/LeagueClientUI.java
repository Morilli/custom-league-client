package com.hawolt;

import com.hawolt.async.ExecutorManager;
import com.hawolt.async.loader.PreferenceLoader;
import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.async.rms.GameStartListener;
import com.hawolt.authentication.LocalCookieSupplier;
import com.hawolt.client.IClientCallback;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.RiotClient;
import com.hawolt.client.cache.CacheType;
import com.hawolt.client.misc.ClientConfiguration;
import com.hawolt.generic.token.impl.StringTokenSupplier;
import com.hawolt.io.RunLevel;
import com.hawolt.logger.Logger;
import com.hawolt.manifest.RMANCache;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.rtmp.amf.decoder.AMFDecoder;
import com.hawolt.settings.*;
import com.hawolt.shutdown.ShutdownManager;
import com.hawolt.ui.MainUI;
import com.hawolt.ui.chat.ChatSidebar;
import com.hawolt.ui.chat.friendlist.ChatSidebarFriendlist;
import com.hawolt.ui.chat.window.ChatUI;
import com.hawolt.ui.layout.LayoutManager;
import com.hawolt.ui.login.ILoginCallback;
import com.hawolt.ui.login.LoginUI;
import com.hawolt.ui.settings.SettingsUI;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.virtual.client.RiotClientException;
import com.hawolt.virtual.leagueclient.exception.LeagueException;
import com.hawolt.virtual.leagueclient.userinfo.UserInformation;
import com.hawolt.virtual.riotclient.instance.MultiFactorSupplier;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.EventListener;
import com.hawolt.xmpp.event.EventType;
import com.hawolt.xmpp.event.objects.other.PlainData;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created: 11/08/2023 18:10
 * Author: Twitter @hawolt
 **/

public class LeagueClientUI extends JFrame implements IClientCallback, ILoginCallback, WindowStateListener, ResourceConsumer<JSONObject, byte[]> {
    public static final ExecutorService service = ExecutorManager.registerService("pool", Executors.newCachedThreadPool());
    private static BufferedImage logo;

    static {
        // DISABLE LOGGING USER CREDENTIALS
        StringTokenSupplier.debug = false;
        AMFDecoder.debug = false;
        try {
            logo = ImageIO.read(RunLevel.get("logo.png"));
        } catch (IOException e) {
            Logger.error("Failed to load {} logo", StaticConstant.PROJECT);
        }
    }

    private ShutdownManager shutdownManager;
    private LeagueClient leagueClient;
    private RiotClient riotClient;
    private SettingService settingService;
    private ChatSidebar chatSidebar;
    private LayoutManager manager;
    private ChatUI chatUI;
    private SettingsUI settingsUI;
    private LoginUI loginUI;
    private MainUI mainUI;

    public LeagueClientUI(String title) {
        super(title);
        this.addWindowStateListener(this);
        this.addWindowListener(new WindowCloseHandler(this));
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    public static void main(String[] args) {
        RMANCache.preload();
        LeagueClientUI leagueClientUI = new LeagueClientUI(StaticConstant.PROJECT);
        leagueClientUI.setIconImage(logo);
        leagueClientUI.settingService = new SettingManager();
        ClientSettings clientSettings = leagueClientUI.settingService.getClientSettings();
        if (clientSettings.isRememberMe()) {
            UserSettings userSettings = leagueClientUI.settingService.set(clientSettings.getRememberMeUsername());
            LocalCookieSupplier localCookieSupplier = new LocalCookieSupplier();
            localCookieSupplier.loadCookieState(userSettings.getCookies());
            ClientConfiguration configuration = ClientConfiguration.getDefault(localCookieSupplier);
            leagueClientUI.createRiotClient(configuration);
        } else {
            leagueClientUI.loginUI = LoginUI.show(leagueClientUI);
            leagueClientUI.setVisible(true);
        }
    }

    private void configure(boolean remember) {
        ResourceLoader.loadResource("local", new PreferenceLoader(leagueClient), true, this);
        if (!remember) return;
        this.settingService.write(
                SettingType.PLAYER,
                "cookies",
                leagueClient.getVirtualRiotClientInstance().getCookieSupplier().getCurrentCookieState()
        );
    }

    private void bootstrap(LeagueClient client) {
        this.leagueClient = client;
        this.shutdownManager = new ShutdownManager(client);
        this.configure(loginUI == null || loginUI.getRememberMe().isSelected());
    }

    @Override
    public void consume(Object o, JSONObject object) {
        if (!object.has("partiesPositionPreferences") || object.isNull("partiesPositionPreferences")) {
            JSONObject partiesPositionPreferences = new JSONObject();
            JSONObject data = new JSONObject();
            String firstPreference = "UNSELECTED";
            String secondPreference = "UNSELECTED";
            data.put("firstPreference", firstPreference);
            data.put("secondPreference", secondPreference);
            partiesPositionPreferences.put("data", data);
            object.put("partiesPositionPreferences", partiesPositionPreferences);
        }
        leagueClient.cache(CacheType.PLAYER_PREFERENCE, object);
        this.settingService.write(SettingType.PLAYER, "preferences", object);
        this.buildUI(leagueClient);
        this.wrap();
    }

    private void wrap() {
        this.setVisible(true);
        this.leagueClient.getRMSClient().getHandler().addMessageServiceListener(MessageService.GSM, new GameStartListener(this));
        VirtualRiotXMPPClient xmppClient = leagueClient.getXMPPClient();
        RMANCache.purge();
        this.chatUI.setSupplier(xmppClient);
        xmppClient.addHandler(
                EventType.ON_READY,
                (EventListener<PlainData>) event -> buildSidebarUI(xmppClient, chatUI)
        );
        xmppClient.connect();
    }

    @Override
    public void onClient(LeagueClient client) {
        this.bootstrap(client);
    }

    private void buildUI(LeagueClient client) {
        mainUI = new MainUI(this);
        ChildUIComponent temporary = new ChildUIComponent(new BorderLayout());
        chatUI = new ChatUI();
        chatUI.setVisible(false);
        mainUI.addChatComponent(chatUI);
        settingsUI = new SettingsUI(this);
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
        mainUI.setMainComponent(temporary);
        mainUI.revalidate();
    }

    private void buildSidebarUI(VirtualRiotXMPPClient xmppClient, ChatUI chatWindow) {
        chatSidebar.getProfile().getSummoner().getStatus().setXMPPClient(xmppClient);
        ChatSidebarFriendlist friendlist = chatSidebar.getChatSidebarFriendlist();
        xmppClient.addMessageListener(getLayoutManager().getChampSelect());
        xmppClient.addMessageListener(chatWindow);
        friendlist.onEvent(xmppClient.getFriendList());
        xmppClient.addPresenceListener(friendlist);
        xmppClient.addFriendListener(friendlist);
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

    public SettingService getSettingService() {
        return settingService;
    }

    public ShutdownManager getShutdownManager() {
        return shutdownManager;
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
        if (throwable instanceof RiotClientException e) {
            switch (e.getMessage()) {
                case "ERROR_TYPE_IS_NULL" -> showFailureDialog("Login errored but the error returned is null");
                case "CAPTCHA_NOT_SUCCESSFUL" -> showFailureDialog("Our Captcha was denied");
                case "UNKNOWN_RESPONSE" -> showFailureDialog("Unable to tell what is wrong");
                case "MISSING_TYPE" -> showFailureDialog("Unable to tell login stage type");
                case "AUTH_FAILURE" -> showFailureDialog("Invalid username or password");
                case "RATE_LIMITED" -> showFailureDialog("You are being rate limited");
                case "UNKNOWN" -> showFailureDialog("If you see this Riot is burning");
                case "CLOUDFLARE" -> showFailureDialog("Temporary Cloudflare block");
                default -> showFailureDialog("Unhandled RiotClientException");
            }
        } else if (throwable instanceof LeagueException e) {
            switch (e.getType()) {
                case NO_LEAGUE_ACCOUNT -> showFailureDialog("No League account connected");
                case NO_SUMMONER_NAME -> showFailureDialog("No name set for summoner");
                default -> showFailureDialog("Unhandled LeagueClientException");
            }
        } else if (throwable instanceof IOException) {
            switch (throwable.getMessage()) {
                case "PREFERENCE_FAILURE" -> showFailureDialog("Unable to load Player Preference");
                default -> showFailureDialog("Unhandled IOException");
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

    @Override
    public void onException(Object o, Exception e) {
        this.onLoginFlowException(new IOException("PREFERENCE_FAILURE"));
    }

    @Override
    public JSONObject transform(byte[] bytes) throws Exception {
        return new JSONObject(new String(bytes));
    }
}
