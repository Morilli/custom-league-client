package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.util.ColorPalette;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * Created: 29/08/2023 17:11
 * Author: Twitter @hawolt
 **/

public class ChampSelectDebugUI extends ChampSelectUIComponent {
    private final JLabel queueId, counter, phaseName;

    public ChampSelectDebugUI() {
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.setLayout(new GridLayout(0, 3, 5, 0));
        this.setBorder(
                BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 1, 1, 1, Color.BLACK),
                        new EmptyBorder(2, 5, 2, 5)
                )
        );
        this.add(queueId = new JLabel("", SwingConstants.LEFT));
        this.queueId.setForeground(Color.WHITE);
        this.add(phaseName = new JLabel("", SwingConstants.CENTER));
        this.phaseName.setForeground(Color.WHITE);
        this.add(counter = new JLabel("", SwingConstants.RIGHT));
        this.counter.setForeground(Color.WHITE);
    }

    @Override
    public void update() {
        this.queueId.setText(String.format("Queue ID: %s", index.getQueueId()));
        this.counter.setText(String.format("COUNTER: %s", index.getCounter()));
        this.phaseName.setText(String.format("PHASE: %s - %s", index.getPhaseName(), index.getSubphase()));
    }

}
