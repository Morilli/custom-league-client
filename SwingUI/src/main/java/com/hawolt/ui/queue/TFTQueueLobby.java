package com.hawolt.ui.queue;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.resources.Loadout;
import com.hawolt.client.resources.ledge.LedgeEndpoint;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.parties.objects.PartiesRegistration;
import com.hawolt.client.resources.ledge.parties.objects.PartyException;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyAction;
import com.hawolt.client.resources.ledge.parties.objects.data.PositionPreference;
import com.hawolt.client.resources.ledge.parties.objects.data.TFTLegend;
import com.hawolt.client.resources.ledge.summoner.SummonerLedge;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.logger.Logger;
import com.hawolt.util.panel.ChildUIComponent;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created: 11/08/2023 23:00
 * Author: Twitter @hawolt
 **/

public class TFTQueueLobby extends ChildUIComponent {
    public TFTQueueLobby(LeagueClientUI leagueClientUI, Container parent, CardLayout layout) {
        super(new BorderLayout());
        Loadout loadout = new Loadout(leagueClientUI.getLeagueClient());
        JButton close = new JButton("Return to previous Component");
        close.addActionListener(listener -> layout.show(parent, "modes"));
        add(close, BorderLayout.NORTH);
        ChildUIComponent component = new ChildUIComponent(new BorderLayout());
        JButton invite = new JButton("Invite another Summoner");
        invite.addActionListener(listener -> {
            String name = (String) JOptionPane.showInputDialog(this, "Summonername", "Invite", JOptionPane.PLAIN_MESSAGE, null, null, "");
            if (name == null) return;
            LedgeEndpoint ledges = leagueClientUI.getLeagueClient().getLedge();
            SummonerLedge summonerLedge = ledges.getSummoner();
            PartiesLedge partiesLedge = ledges.getParties();
            try {
                Summoner summoner = summonerLedge.resolveSummonerByName(name);
                partiesLedge.invite(summoner.getPUUID());
            } catch (IOException | PartyException e) {
                Logger.error(e);
            }
        });
        component.add(invite, BorderLayout.NORTH);
        ChildUIComponent roles = new ChildUIComponent(new GridLayout(0, 1, 5, 0));
        JComboBox<TFTLegend> main = new JComboBox<>(TFTLegend.values());
        AtomicInteger index = new AtomicInteger();
        try {
            switch (loadout.getLegend()) {
                case 1 -> index.set(0);
                case 15 -> index.set(1);
                case 16 -> index.set(2);
                case 17 -> index.set(3);
                case 18 -> index.set(4);
                case 19 -> index.set(5);
                case 20 -> index.set(6);
                case 21 -> index.set(7);
                case 22 -> index.set(8);
                case 23 -> index.set(9);
                case 24 -> index.set(10);
                case 25 -> index.set(11);
                case 26 -> index.set(12);
                case 27 -> index.set(13);
                case 28 -> index.set(14);
            }
        } catch (IOException e) {
            Logger.error(e);
            index.set(1);
        }
        main.setSelectedIndex(index.get());
        try {
            loadout.getLoadout();
        } catch (IOException e) {
            Logger.error(e);
        }
        roles.add(main);
        component.add(roles, BorderLayout.SOUTH);
        add(component, BorderLayout.CENTER);
        ChildUIComponent bottom = new ChildUIComponent(new GridLayout(0, 1, 0, 0));
        JButton start = new JButton("START QUEUE");
        AtomicInteger atomicItemId = new AtomicInteger();
        start.addActionListener(listener -> {
            switch (main.getItemAt(main.getSelectedIndex())) {
                case PORO -> atomicItemId.set(1);
                case VEIGAR -> atomicItemId.set(15);
                case TAHM -> atomicItemId.set(16);
                case VLADIMIR -> atomicItemId.set(17);
                case EZREAL -> atomicItemId.set(18);
                case DRAVEN -> atomicItemId.set(19);
                case TF -> atomicItemId.set(20);
                case URF -> atomicItemId.set(21);
                case ORNN -> atomicItemId.set(22);
                case CATILYN -> atomicItemId.set(23);
                case PENGU -> atomicItemId.set(24);
                case YI -> atomicItemId.set(25);
                case LEE -> atomicItemId.set(26);
                case AURELION -> atomicItemId.set(27);
                case BARD -> atomicItemId.set(28);
            }
            try {
                loadout.setLegend(atomicItemId.get(), leagueClientUI.getLeagueClient().getLedge().getInventoryService().getLegendInstanceId(atomicItemId.get()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            PartiesLedge partiesLedge = leagueClientUI.getLeagueClient().getLedge().getParties();
            PartiesRegistration registration = partiesLedge.getCurrentRegistration();
            try {
                if (registration == null) partiesLedge.register();
                PositionPreference primary = PositionPreference.TOP;
                PositionPreference secondary = PositionPreference.TOP;
                partiesLedge.metadata(primary, secondary);
                partiesLedge.setQueueAction(PartyAction.START);
            } catch (IOException e) {
                Logger.error(e);
            }
        });
        bottom.add(start);
        JButton stop = new JButton("STOP QUEUE");
        stop.addActionListener(listener -> {
            PartiesLedge partiesLedge = leagueClientUI.getLeagueClient().getLedge().getParties();
            PartiesRegistration registration = partiesLedge.getCurrentRegistration();
            try {
                if (registration == null) return;
                partiesLedge.setQueueAction(PartyAction.STOP);
            } catch (IOException e) {
                Logger.error(e);
            }
        });
        bottom.add(stop);
        add(bottom, BorderLayout.SOUTH);
    }
}
