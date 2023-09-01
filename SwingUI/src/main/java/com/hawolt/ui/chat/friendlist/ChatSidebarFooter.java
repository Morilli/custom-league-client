package com.hawolt.ui.chat.friendlist;

import com.hawolt.ui.github.Github;
import com.hawolt.ui.settings.SettingsUI;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.util.ui.FlatButton;
import com.hawolt.util.ui.HighlightType;
import com.hawolt.util.ui.Label;
import com.hawolt.util.ui.TextAlign;

import javax.swing.*;
import java.awt.*;

public class ChatSidebarFooter extends ChildUIComponent {
    private static final Font font = new Font("", Font.BOLD, 20);
    private static final int HEIGHT = 30;

    public ChatSidebarFooter(SettingsUI settingsWindow) {
        super(new BorderLayout());
        this.setPreferredSize(new Dimension(0, HEIGHT));
        this.setBackground(ColorPalette.BACKGROUND_COLOR);

        FlatButton settingsButton = new FlatButton("⚙", TextAlign.CENTER, HighlightType.COMPONENT);
        settingsButton.setBorder(BorderFactory.createEmptyBorder());
        settingsButton.setFont(font);
        settingsButton.setPreferredSize(new Dimension(HEIGHT, HEIGHT));
        settingsButton.addActionListener(listener -> {
            if (settingsWindow.isVisible()) {
                settingsWindow.close();
            } else {
                settingsWindow.setVisible(true);
            }
        });
        add(settingsButton, BorderLayout.EAST);
        Label version = new Label(Github.getVersion(), TextAlign.CENTER, true);
        add(version, BorderLayout.CENTER);
    }
}