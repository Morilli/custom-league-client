package com.hawolt.ui.chat.friendlist;

import com.hawolt.LeagueClientUI;
import com.hawolt.ui.chat.window.IChatWindow;
import com.hawolt.util.AudioEngine;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.util.ui.FlatButton;
import com.hawolt.util.ui.HighlightType;
import com.hawolt.util.ui.Label;
import com.hawolt.util.ui.TextAlign;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.EventListener;
import com.hawolt.xmpp.event.handler.presence.IPresenceListener;
import com.hawolt.xmpp.event.objects.friends.FriendList;
import com.hawolt.xmpp.event.objects.friends.GenericFriend;
import com.hawolt.xmpp.event.objects.friends.IFriendListener;
import com.hawolt.xmpp.event.objects.friends.impl.OnlineFriend;
import com.hawolt.xmpp.event.objects.friends.status.FailedFriendStatus;
import com.hawolt.xmpp.event.objects.presence.AbstractPresence;
import com.hawolt.xmpp.event.objects.presence.impl.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Created: 08/08/2023 18:11
 * Author: Twitter @hawolt
 **/

public class ChatSidebarFriendlist extends ChildUIComponent implements IFriendListener, IPresenceListener, IFriendListComponent, EventListener<FriendList> {
    private final Map<String, ChatSidebarFriend> map = new HashMap<>();
    private final IChatWindow window;
    private JComponent component;
    private LeagueClientUI leagueClientUI;
    private Map<GenericFriend, ChildUIComponent> tmp = new HashMap<>();

    public ChatSidebarFriendlist(IChatWindow window, LeagueClientUI leagueClientUI) {
        super(new GridLayout(0, 1, 0, 0));
        this.window = window;
        this.window.setIFriendListComponent(this);
        this.leagueClientUI = leagueClientUI;
        setBackground(ColorPalette.BACKGROUND_COLOR);
    }

    @Override
    public void search(String name) {
        List<ChatSidebarFriend> list = new ArrayList<>(map.values());
        for (ChatSidebarFriend sidebar : list) {
            GenericFriend friend = sidebar.getFriend();
            String computed = friend instanceof OnlineFriend ?
                    ((OnlineFriend) friend).getLOLName() :
                    String.join(
                            "#",
                            friend.getName().toString(),
                            friend.getTagline().toString()
                    );
            if (name.isEmpty() || computed.toLowerCase().contains(name)) {
                sidebar.setEnabled(true);
                add(sidebar);
            } else {
                sidebar.setEnabled(false);
                remove(sidebar);
            }
        }
        revalidate();
    }

    @Override
    public void registerNotificationBar(JComponent component) {
        this.component = component;
    }

    @Override
    public void onMessage(String jid) {
        if (window.isChatOpened(jid)) return;
        if (!map.containsKey(jid)) return;
        this.getComponent(jid).increment();
    }

    @Override
    public void onOpen(String jid) {
        this.getComponent(jid).opened();
    }

    public void addFriendComponent(ChatSidebarFriend component) {
        this.map.put(component.getFriend().getJID(), component);
        this.add(component);
    }

    public void removeFriendComponent(String jid) {
        if (!map.containsKey(jid)) return;
        ChatSidebarFriend friend = map.get(jid);
        map.remove(jid);
        remove(friend);
        revalidate();
    }

    private ChatSidebarFriend getComponent(String jid) {
        return map.get(jid);
    }

    private void handle(AbstractPresence presence) {
        if (!map.containsKey(presence.getFrom())) return;
        getComponent(presence.getFrom()).setLastKnownPresence(presence);
        List<ChatSidebarFriend> collection = new ArrayList<>(map.values());
        collection.sort(Comparator.comparingInt(o -> o.getConnectionStatus().ordinal()));
        Collections.reverse(collection);
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof ChatSidebarFriend) {
                remove(component);
            }
        }
        for (ChatSidebarFriend friend : collection) {
            add(friend);
        }
        revalidate();
    }

    private void addFriendComponent(GenericFriend friend) {
        ChatSidebarFriend button = new ChatSidebarFriend(window.getXMPPClient(), friend, leagueClientUI);
        button.executeOnClick(() -> window.showChat(friend));
        addFriendComponent(button);
    }

    //TODO ah no no no
    private void addPendingFriendRequest(GenericFriend genericFriend, boolean incoming) {
        ChildUIComponent request = new ChildUIComponent(new BorderLayout(5, 0));
        request.setBackground(ColorPalette.BACKGROUND_COLOR);
        Label name = new Label(genericFriend.getName().toString(), TextAlign.LEFT, true);
        request.add(name, BorderLayout.CENTER);
        request.setPreferredSize(new Dimension(0, 30));
        ChildUIComponent actions = new ChildUIComponent(new GridLayout(0, incoming ? 2 : 1, 5, 0));
        actions.setBackground(ColorPalette.BACKGROUND_COLOR);
        if (incoming) {
            FlatButton accept = new FlatButton("+", TextAlign.CENTER, HighlightType.COMPONENT);
            accept.addActionListener(listener -> {
                VirtualRiotXMPPClient client = window.getXMPPClient();
                List<GenericFriend> list = client.getFriendList().find(friend -> genericFriend.getName().equals(friend.getName()));
                if (list.isEmpty()) return;
                client.addFriendByTag(list.get(0).getName().toString(), list.get(0).getTagline().toString());
                component.remove(request);
                component.revalidate();
            });
            accept.setPreferredSize(new Dimension(30, 0));
            actions.add(accept);
        }
        FlatButton remove = new FlatButton("×", TextAlign.CENTER, HighlightType.COMPONENT);
        remove.addActionListener(listener -> {
            VirtualRiotXMPPClient client = window.getXMPPClient();
            List<GenericFriend> list = client.getFriendList().find(friend -> genericFriend.getName().equals(friend.getName()));
            if (list.isEmpty()) return;
            client.removeFriend(list.get(0).getJID());
            component.remove(request);
            component.revalidate();
        });
        remove.setPreferredSize(new Dimension(30, 0));
        actions.add(remove);
        tmp.put(genericFriend, request);
        request.add(actions, BorderLayout.EAST);
        this.component.add(request);
    }


    @Override
    public void onFriendRemove(GenericFriend genericFriend) {
        removeFriendComponent(genericFriend.getJID());
    }

    @Override
    public void onIncomingFriendRequest(GenericFriend genericFriend) {
        if (this.component == null) return;
        AudioEngine.play("buddy_invite.wav");
        addPendingFriendRequest(genericFriend, true);
        component.revalidate();
    }

    @Override
    public void onOutgoingFriendRequest(GenericFriend genericFriend) {
        if (this.component == null) return;
        addPendingFriendRequest(genericFriend, false);
        component.revalidate();
    }

    @Override
    public void onIncomingFriendRequestRevoked(GenericFriend genericFriend) {

    }

    @Override
    public void onIncomingFriendRequestAccepted(GenericFriend genericFriend) {
        addFriendComponent(genericFriend);
        revalidate();
    }

    @Override
    public void onOutgoingFriendRequestCanceled(GenericFriend genericFriend) {
        if (tmp.containsKey(genericFriend)) {
            component.remove(tmp.get(genericFriend));
            component.revalidate();
        }
    }

    @Override
    public void onOutgoingFriendRequestAccepted(GenericFriend genericFriend) {
        if (tmp.containsKey(genericFriend)) {
            component.remove(tmp.get(genericFriend));
            component.revalidate();
        }
        addFriendComponent(genericFriend);
        revalidate();
    }

    @Override
    public void onFailedInteraction(FailedFriendStatus failedFriendStatus) {

    }

    @Override
    public void onGamePresence(GamePresence gamePresence) {
        handle(gamePresence);
    }

    @Override
    public void onOnlinePresence(OnlinePresence onlinePresence) {
        handle(onlinePresence);
    }

    @Override
    public void onMobilePresence(MobilePresence mobilePresence) {
        handle(mobilePresence);
    }

    @Override
    public void onOfflinePresence(OfflinePresence offlinePresence) {
        handle(offlinePresence);
    }

    @Override
    public void onDeceivePresence(DeceivePresence deceivePresence) {
        handle(deceivePresence);
    }

    @Override
    public void onUnknownPresence(AbstractPresence abstractPresence) {
        handle(abstractPresence);
    }

    @Override
    public void onFakeMobilePresence(FakeMobilePresence fakeMobilePresence) {
        handle(fakeMobilePresence);
    }

    @Override
    public void onEvent(FriendList friendList) {
        for (GenericFriend friend : friendList.get()) {
            switch (friend.getType()) {
                case BOTH -> addFriendComponent(friend);
                case PENDING_IN -> addPendingFriendRequest(friend, true);
                case PENDING_OUT -> addPendingFriendRequest(friend, false);
            }
        }
        if (component != null) component.revalidate();
        revalidate();
    }
}
