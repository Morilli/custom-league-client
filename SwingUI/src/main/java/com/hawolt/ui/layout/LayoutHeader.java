package com.hawolt.ui.layout;

import com.hawolt.client.LeagueClient;
import com.hawolt.ui.layout.wallet.HeaderWallet;
import com.hawolt.util.AudioEngine;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.util.ui.FlatButton;
import com.hawolt.util.ui.HighlightType;
import com.hawolt.util.ui.TextAlign;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Created: 09/08/2023 15:52
 * Author: Twitter @hawolt
 **/

public class LayoutHeader extends ChildUIComponent {
    private final HeaderWallet wallet;

    public LayoutHeader(ILayoutManager manager, LeagueClient client) {
        super(new GridLayout(0, 5, 5, 0));
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.setPreferredSize(new Dimension(0, 90));
        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        FlatButton button = new FlatButton("STORE", TextAlign.CENTER, HighlightType.BOTTOM);
        FlatButton button1 = new FlatButton("PLAY", TextAlign.CENTER, HighlightType.BOTTOM);
        FlatButton button2 = new FlatButton("CHAMPSELECT", TextAlign.CENTER, HighlightType.BOTTOM);
        FlatButton button3 = new FlatButton("RUNES", TextAlign.CENTER, HighlightType.BOTTOM);

        final Consumer<FlatButton> selectButton = (b) -> {
            button.setSelected(false);
            button1.setSelected(false);
            button2.setSelected(false);
            button3.setSelected(false);
            b.setSelected(true);
        };

        button.addActionListener(o -> {
            selectButton.accept(button);
            AudioEngine.play("openstore.wav");
            manager.showComponent("store");
        });
        add(button);
        button1.addActionListener(o -> {
            selectButton.accept(button1);
            manager.showComponent("play");
        });
        add(button1);
        button2.addActionListener(o -> {
            selectButton.accept(button2);
            manager.showComponent("select");
        });
        add(button2);
        button3.addActionListener(o -> {
            selectButton.accept(button3);
            manager.showComponent("runes");
        });

        add(button3);
        add(wallet = new HeaderWallet(client));
    }

    public HeaderWallet getWallet() {
        return wallet;
    }
}
