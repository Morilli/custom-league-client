package com.hawolt.ui.runes;

import com.hawolt.util.ColorPalette;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Created: 15/08/2023 21:15
 * Author: Twitter @hawolt
 **/

public class RunePanel extends JPanel implements IRuneSelection {
    private final RunePanelRow[] components;
    private final boolean secondary, special;
    private final DDRuneType type;

    private int previous = -1, last = -1;

    public RunePanel(DDRuneType type, boolean secondary) {
        this(type, secondary, true);
    }

    public RunePanel(DDRuneType type, boolean secondary, boolean skip) {
        this.setLayout(new GridLayout(0, 1, 0, 5));
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.special = secondary && skip;
        this.secondary = secondary;
        this.type = type;
        LinkedList<LinkedList<DDRune>> alignment = type.getAlignment();
        this.components = new RunePanelRow[alignment.size()];
        for (int i = 0; i < alignment.size(); i++) {
            if (i == 0 && special) continue;
            Dimension dimension = (i == 0 && skip) ? new Dimension(128, 128) : skip ? new Dimension(64, 64) : new Dimension(48, 48);
            this.components[i] = new RunePanelRow(this, i, alignment.get(i), dimension);
            this.add(components[i]);
        }
    }


    public DDRuneType getType() {
        return type;
    }

    @Override
    public void onSelection(int componentIndex, boolean selection) {
        if (!secondary || !selection) return;
        if (previous == componentIndex) return;
        int resetTarget = last;
        this.last = previous;
        this.previous = componentIndex;
        if (resetTarget != -1 && resetTarget != componentIndex) {
            components[resetTarget].reset(-1);
        }
    }

    public DDRune[] getSelectedRunes() throws IncompleteRunePageException {
        System.out.println(type.getName());
        DDRune[] runes = new DDRune[secondary ? 2 : components.length];
        int helperIndex = 0;
        for (int i = 0; i < components.length; i++) {
            RunePanelRow row = components[i];
            if (row == null || row.getLastSelection() == -1) continue;
            RunePanelRune rune = row.getPanelComponents()[row.getLastSelection()];
            runes[secondary ? helperIndex++ : i] = rune.getRune();
        }
        for (int i = 0; i < runes.length; i++) {
            if (runes[i] == null) {
                throw new IncompleteRunePageException("UNDEFINED_RUNE_AT_INDEX" + i + ":" + secondary);
            }
        }
        return runes;
    }
}
