package com.hawolt.ui.chat.profile;

import com.hawolt.util.ColorPalette;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.virtual.leagueclient.userinfo.UserInformation;

import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created: 08/08/2023 17:42
 * Author: Twitter @hawolt
 **/

public class ChatSidebarSummoner extends ChildUIComponent {
    private final ChatSidebarStatus status;
    private final ChatSidebarName name;
    private final ChatSidebarXP bar;

    public ChatSidebarSummoner(UserInformation information, LayoutManager layout) {
        super(layout);
        this.setBorder(new EmptyBorder(0, 5, 0, 0));
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        add(name = new ChatSidebarName());
        add(bar = new ChatSidebarXP(information));
        add(status = new ChatSidebarStatus());
    }

    public ChatSidebarName getChatSidebarName() {
        return name;
    }

    public ChatSidebarXP getChatSidebarBar() {
        return bar;
    }

    public ChatSidebarStatus getStatus() {
        return status;
    }
}
