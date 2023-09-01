package com.hawolt.ui.settings;

import com.hawolt.LeagueClientUI;
import com.hawolt.settings.SettingService;
import com.hawolt.util.panel.ChildUIComponent;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SettingsUI extends ChildUIComponent {
    private static final Font font = new Font("Arial", Font.PLAIN, 20);
    private final List<SettingsPage> pages = new ArrayList<>();
    private final LeagueClientUI leagueClientUI;
    private final SettingsSidebar sidebar;

    public SettingsUI(LeagueClientUI leagueClientUI) {
        super(new BorderLayout());
        this.leagueClientUI = leagueClientUI;
        setBorder(BorderFactory.createTitledBorder(
                        new MatteBorder(2, 2, 2, 2, Color.DARK_GRAY)
                )
        );
        ChildUIComponent header = new ChildUIComponent(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 40));
        JLabel label = new JLabel("Settings");
        label.setFont(font);
        label.setHorizontalAlignment(JLabel.CENTER);
        header.add(label, BorderLayout.CENTER);

        //TODO CardLayout for pages
        SettingsPage clientGeneralPage = newClientGeneralPage();
        add(clientGeneralPage);

        add(header, BorderLayout.NORTH);

        //Sidebar
        sidebar = new SettingsSidebar();
        add(sidebar, BorderLayout.WEST);

        SettingsSidebar.GroupTab clientGroup = sidebar.addGroupTab("Client");

        JButton clientGeneralButton = SettingsSidebar.newSectionButton("General");
        clientGroup.addToContainer(clientGeneralButton);

        //Footer
        ChildUIComponent footer = new ChildUIComponent(new FlowLayout(FlowLayout.CENTER, 5, 5));
        add(footer, BorderLayout.SOUTH);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(listener -> {
            save();
        });
        footer.add(saveButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(listener -> {
            close();
        });
        footer.add(closeButton);
        revalidate();
    }

    public void save() {
        for (SettingsPage page : pages) {
            page.save();
        }
    }

    public void close() {
        for (SettingsPage page : pages) {
            page.close();
        }
        this.setVisible(false);
    }

    public void add(SettingsPage page) {
        pages.add(page);
        add(page, BorderLayout.CENTER);
    }

    private SettingsPage newClientGeneralPage() {
        SettingService service = leagueClientUI.getSettingService();
        SettingsPage result = new SettingsPage();
        result.add(SettingUIComponent.createTagComponent("Path"));
        result.add(SettingUIComponent.createPathComponent("League Base Directory Path", service, "GameBaseDir"));
        return result;
    }
}
