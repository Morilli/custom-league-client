package com.hawolt.ui.settings;

import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.util.ui.DynamicGridLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SettingsPage extends ChildUIComponent {
    private final List<SettingUIComponent> componentList = new ArrayList<>();
    private final ChildUIComponent container;

    public SettingsPage() {
        super(new BorderLayout(0, 5));

        ChildUIComponent component = new ChildUIComponent(new BorderLayout());

        container = new ChildUIComponent(new DynamicGridLayout(0, 1, 0, 0));

        super.add(component, BorderLayout.NORTH);
        component.add(container, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        super.add(scrollPane, BorderLayout.CENTER);
    }

    public void add(JComponent component) {
        container.add(component, BorderLayout.NORTH);
    }

    public void add(SettingUIComponent component) {
        componentList.add(component);
        container.add(component, BorderLayout.NORTH);
    }

    public void save() {
        for (SettingUIComponent component : componentList) {
            component.save();
        }
    }

    public void close() {
        for (SettingUIComponent component : componentList) {
            component.close();
        }
    }
}
