package com.hawolt.ui.chat.friendlist;

import com.hawolt.LeagueClientUI;
import com.hawolt.ui.queue.GameInvites;
import com.hawolt.ui.queue.QueueState;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * Created: 09/08/2023 01:43
 * Author: Twitter @hawolt
 **/

public class ChatSidebarEssentials extends ChildUIComponent {
    private final QueueState state;

    public ChatSidebarEssentials(LeagueClientUI leagueClientUI, IFriendListComponent component) {
        super(new BorderLayout());
        this.setBorder(
                BorderFactory.createCompoundBorder(
                        new MatteBorder(2, 0, 2, 0, Color.DARK_GRAY),
                        new EmptyBorder(0, 5, 5, 5)
                )
        );
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        VirtualRiotXMPPClient xmppClient = leagueClientUI.getLeagueClient().getXMPPClient();
        ChatSidebarFriendEssentials essentials = new ChatSidebarFriendEssentials(xmppClient, component);
        this.add(essentials, BorderLayout.CENTER);
        this.state = new QueueState();
        this.state.setVisible(false);
        ChildUIComponent display = new ChildUIComponent(new BorderLayout());
        display.setBackground(ColorPalette.BACKGROUND_COLOR);
        display.add(state, BorderLayout.NORTH);
        display.add(new GameInvites(leagueClientUI), BorderLayout.CENTER);
        this.add(display, BorderLayout.NORTH);
        ChildUIComponent requests = new ChildUIComponent(new GridLayout(0, 1, 0, 5));
        requests.setBorder(new EmptyBorder(5, 0, 0, 0));
        requests.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.add(requests, BorderLayout.SOUTH);
        component.registerNotificationBar(requests);
    }

    public void toggleQueueState(long currentTimeMillis, long estimatedMatchmakingTimeMillis) {
        toggleQueueState(currentTimeMillis, estimatedMatchmakingTimeMillis, false);
    }

    public void toggleQueueState(long currentTimeMillis, long estimatedMatchmakingTimeMillis, boolean lpq) {
        if (state.isLPQ()) state.updateLPQ(estimatedMatchmakingTimeMillis);
        state.setTimer(currentTimeMillis, estimatedMatchmakingTimeMillis, lpq);
        state.setVisible(true);
        revalidate();
    }

    public void disableQueueState() {
        state.setVisible(false);
        revalidate();
    }


}
