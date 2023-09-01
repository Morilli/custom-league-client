package com.hawolt.ui.layout;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.ui.champselect.ChampSelect;
import com.hawolt.ui.queue.QueueWindow;
import com.hawolt.ui.runes.RuneSelectionPanel;
import com.hawolt.ui.store.StoreWindow;
import com.hawolt.util.panel.ChildUIComponent;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * Created: 09/08/2023 15:48
 * Author: Twitter @hawolt
 **/

public class LayoutManager extends ChildUIComponent implements ILayoutManager {

    private final CardLayout layout = new CardLayout();
    private final ChildUIComponent center;
    private final LayoutHeader header;


    private final ChampSelect champSelect;
    private final QueueWindow queue;
    private final StoreWindow store;
    private final RuneSelectionPanel runes;

    public LayoutManager(LeagueClientUI leagueClientUI) {
        super(new BorderLayout());

        LeagueClient client = leagueClientUI.getLeagueClient();

        this.add(header = new LayoutHeader(this, client), BorderLayout.NORTH);
        this.add(center = new ChildUIComponent(layout), BorderLayout.CENTER);
        this.center.setBorder(new MatteBorder(2, 0, 0, 0, Color.DARK_GRAY));

        this.center.add("placebo", new JPanel());
        this.center.add("store", store = new StoreWindow(client));
        this.center.add("play", queue = new QueueWindow(leagueClientUI));
        this.center.add("select", champSelect = new ChampSelect(leagueClientUI));
        this.center.add("runes", runes = new RuneSelectionPanel(leagueClientUI));

        layout.show(center, "placebo");
    }

    public void showClientComponent(String name) {
        layout.show(center, name);
    }

    public ChampSelect getChampSelect() {
        return champSelect;
    }

    public QueueWindow getQueue() {
        return queue;
    }

    public RuneSelectionPanel getRunes() {
        return runes;
    }

    public StoreWindow getStore() {
        return store;
    }

    public LayoutHeader getHeader() {
        return header;
    }

    @Override
    public void showComponent(String panel) {
        this.layout.show(center, panel);
    }
}
