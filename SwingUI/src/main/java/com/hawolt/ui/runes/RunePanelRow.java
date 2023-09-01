package com.hawolt.ui.runes;

import com.hawolt.util.ColorPalette;
import com.hawolt.util.panel.ChildUIComponent;

import java.awt.*;
import java.util.LinkedList;

/**
 * Created: 15/08/2023 21:39
 * Author: Twitter @hawolt
 **/

public class RunePanelRow extends ChildUIComponent implements IRuneSelection {
    private final RunePanelRune[] components;
    private final IRuneSelection selection;
    private final int componentIndex;
    private int lastSelection = -1;

    public RunePanelRow(IRuneSelection selection, int componentIndex, LinkedList<DDRune> list, Dimension dimension) {
        this.setLayout(new GridLayout(0, list.size(), 5, 0));
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.selection = selection;
        this.componentIndex = componentIndex;
        this.components = new RunePanelRune[list.size()];
        for (int i = 0; i < list.size(); i++) {
            this.components[i] = new RunePanelRune(this, i, list.get(i), dimension);
            add(this.components[i]);
        }
    }

    public RunePanelRune[] getPanelComponents() {
        return components;
    }

    public int getLastSelection() {
        return lastSelection;
    }

    @Override
    public void onSelection(int componentIndex, boolean selected) {
        if (!selected) {
            this.lastSelection = -1;
        } else {
            this.selection.onSelection(this.componentIndex, true);
            this.reset(componentIndex);
        }
    }

    public void reset(int exception) {
        this.lastSelection = exception;
        for (int i = 0; i < components.length; i++) {
            if (i == exception) continue;
            components[i].setSelected(false);
        }
    }
}
