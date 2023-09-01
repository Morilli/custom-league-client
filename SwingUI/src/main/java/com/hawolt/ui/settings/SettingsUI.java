package com.hawolt.ui.settings;

import com.hawolt.client.settings.client.ClientSettings;
import com.hawolt.client.settings.client.ClientSettingsService;
import com.hawolt.util.panel.ChildUIComponent;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class SettingsUI extends ChildUIComponent {
    private static final Font font = new Font("Arial", Font.PLAIN, 20);
    SettingsSidebar sidebar;
    List<SettingsPage> pageList = new ArrayList<SettingsPage>();
    public SettingsUI() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
                        new MatteBorder(2, 2, 2, 2, Color.DARK_GRAY)
                )
        );
        ChildUIComponent header = new ChildUIComponent (new BorderLayout());
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
        ChildUIComponent footer = new ChildUIComponent(new FlowLayout(FlowLayout.CENTER, 5,5));
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
        for (SettingsPage page : pageList) {
            page.save();
        }
        try {
            ClientSettingsService.get().writeSettingsFile();
        } catch (IOException e) {}
    }

    public void close() {
        for (SettingsPage page : pageList) {
            page.close();
        }
        this.setVisible(false);
    }

    public void add(SettingsPage page) {
        pageList.add(page);
        add(page, BorderLayout.CENTER);
    }

    private SettingsPage newClientGeneralPage() {
        ClientSettings settings = ClientSettingsService.get().getSettings();
        SettingsPage result = new SettingsPage();
        result.add(SettingUIComponent.createTagComponent("Path"));
        result.add(SettingUIComponent.createPathComponent("League Base Directory Path", settings, ClientSettings.Key.GAME_BASE_DIRECTORY.get()));
        return result;
    }
}
