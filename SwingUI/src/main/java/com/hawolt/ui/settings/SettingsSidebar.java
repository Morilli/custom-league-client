package com.hawolt.ui.settings;

import com.hawolt.util.panel.ChildUIComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class SettingsSidebar extends JPanel {
    private static final Font headerFont = new Font("Arial", Font.BOLD, 20);
    private static final Font sectionFont = new Font("Arial", Font.ITALIC, 20);
    private static final Dimension sidebarDimension = new Dimension(200, 0);
    private static final Dimension headerDimension = new Dimension(190, 40);
    private static final Dimension sectionDimension = new Dimension(190, 40);
    JPanel panel;

    public SettingsSidebar() {
        super();
        this.setLayout(new BorderLayout());
        this.setPreferredSize(sidebarDimension);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        new MatteBorder(2, 2, 2, 2, Color.DARK_GRAY),
                        new EmptyBorder(5, 5, 5, 5)
                )
        );
        this.add(panel);
    }

    protected GroupTab addGroupTab(String name) {
        GroupTab result = new GroupTab(name);
        panel.add(result);
        return result;
    }

    protected static JButton newSectionButton(String name) {
        JButton button = new JButton(name);
        button.setFont(sectionFont);
        button.setMaximumSize(sectionDimension);
        return button;
    }

    protected class GroupTab extends JPanel {
        private JPanel container;

        private GroupTab(String name) {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.setPreferredSize(sectionDimension);
            container.setBorder(
                    BorderFactory.createTitledBorder(
                            new MatteBorder(2, 2, 2, 2, Color.DARK_GRAY)
                    )
            );

            JLabel header = new JLabel(name);
            header.setFont(headerFont);
            header.setMaximumSize(headerDimension);

            this.add(header);
            this.add(container);
        }

        protected Component addToContainer(Component component) {
            container.add(component);
            return component;
        }
    }
}
