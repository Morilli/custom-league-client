package com.hawolt.ui.queue;

import com.hawolt.LeagueClientUI;
import com.hawolt.async.ExecutorManager;
import com.hawolt.client.resources.Loadout;
import com.hawolt.client.resources.ledge.LedgeEndpoint;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.parties.objects.*;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyAction;
import com.hawolt.client.resources.ledge.parties.objects.data.TFTLegend;
import com.hawolt.client.resources.ledge.summoner.SummonerLedge;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.logger.Logger;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.util.panel.ChildUIComponent;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Created: 11/08/2023 23:00
 * Author: Twitter @hawolt
 **/

public class TFTQueueLobby extends ChildUIComponent implements IServiceMessageListener<RiotMessageServiceMessage> {

    private final LeagueClientUI leagueClientUI;
    private final ChildUIComponent grid = new ChildUIComponent(new GridLayout(2,4));
    private ScheduledFuture<?> future;

    private final ScheduledExecutorService scheduler = ExecutorManager.getScheduledService("queue-resumer");

    @Override
    public void onMessage(RiotMessageServiceMessage riotMessageServiceMessage) {
        JSONObject payload = riotMessageServiceMessage.getPayload().getPayload();
        PartiesRegistration registration = new PartiesRegistration(payload.getJSONObject("player"));
        String puuid = registration.getPUUID();
        CurrentParty party = registration.getCurrentParty();
        if (party == null) return;
        Logger.error(party);
        List<PartyParticipant> list = party.getPlayers();
        list.stream().filter(participant -> participant.getPUUID().equals(puuid)).findFirst().ifPresent(self -> {
            SummonerLedge summonerLedge = leagueClientUI.getLeagueClient().getLedge().getSummoner();
            try {
                getSummonerComponentAt(0).update(self, summonerLedge.resolveSummonerByPUUD(puuid));
                list.remove(self);
                int memberPosition = 1;
                for (PartyParticipant participant : list) {
                    Summoner summoner = summonerLedge.resolveSummonerByPUUD(participant.getPUUID());
                    if (participant.getRole().equals("MEMBER") || participant.getRole().equals("LEADER")) {
                        getSummonerComponentAt(memberPosition++).update(participant, summoner);
                    }
                }
                for (int i = memberPosition; i < 5; i++) {
                    getSummonerComponentAt(i).update(null, null);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            revalidate();
        });
    }

    private TFTSummonerComponent getSummonerComponentAt(int id) {
        int index;
        if (id == 0) index = 2;
        else if (id == 1) index = 1;
        else if (id == 2) index = 3;
        else if (id == 3) index = 0;
        else index = 4;
        return (TFTSummonerComponent) grid.getComponent(index);
    }




    public TFTQueueLobby(LeagueClientUI leagueClientUI, Container parent, CardLayout layout) {
        super(new BorderLayout());
        this.leagueClientUI = leagueClientUI;
        this.leagueClientUI.getLeagueClient().getRMSClient().getHandler().addMessageServiceListener(MessageService.PARTIES, this);
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
        main.addActionListener(e -> {
            AtomicInteger atomicItemId = new AtomicInteger();
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
            leagueClientUI.service.execute(() -> {
                try {
                    loadout.setLegend(atomicItemId.get(), leagueClientUI.getLeagueClient().getLedge().getInventoryService().getLegendInstanceId(atomicItemId.get()));
                } catch (Exception e2) {
                }
            });
        });
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


        for (int i = 0; i < 8; i++) grid.add(new TFTSummonerComponent());
        grid.setBackground(Color.YELLOW);
        component.add(grid, BorderLayout.CENTER);


        add(component, BorderLayout.CENTER);
        ChildUIComponent bottom = new ChildUIComponent(new GridLayout(0, 1, 0, 0));
        JButton start = new JButton("START QUEUE");
        start.addActionListener(listener -> {

            PartiesLedge partiesLedge = leagueClientUI.getLeagueClient().getLedge().getParties();
            PartiesRegistration registration = partiesLedge.getCurrentRegistration();
            try {
                if (registration == null) partiesLedge.register();
                partiesLedge.setQueueAction(PartyAction.START);
                partiesLedge.ready();
                JSONObject response = partiesLedge.setQueueAction(PartyAction.START);
                List<GatekeeperRestriction> direct = response.has("errorCode") &&
                        "GATEKEEPER_RESTRICTED".equals(response.getString("errorCode")) ?
                        new PartyGatekeeper(response).getRestrictionList() : new ArrayList<>();
                CurrentParty party = partiesLedge.getOwnPlayer().getCurrentParty();
                PartyRestriction restriction = party.getPartyRestriction();
                List<GatekeeperRestriction> indirect = restriction != null ?
                        restriction.getRestrictionList() : new ArrayList<>();
                if (direct.isEmpty() && indirect.isEmpty()) return;
                List<GatekeeperRestriction> sorted = Stream.of(direct, indirect)
                        .flatMap(Collection::stream)
                        .sorted(((o1, o2) -> Long.compare(o2.getRemainingMillis(), o1.getRemainingMillis())))
                        .toList();
                GatekeeperRestriction gatekeeperRestriction = sorted.get(0);
                Logger.debug("Restriction: {}", gatekeeperRestriction);
                leagueClientUI.getChatSidebar().getEssentials().toggleQueueState(
                        System.currentTimeMillis(),
                        gatekeeperRestriction.getRemainingMillis(),
                        true
                );
                future = scheduler.schedule(() -> {
                    try {
                        partiesLedge.resume();
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                }, gatekeeperRestriction.getRemainingMillis(), TimeUnit.MILLISECONDS);
            } catch (IOException e) {
                Logger.error(e);
            }
        });
        bottom.add(start);
        JButton stop = new JButton("STOP QUEUE");
        stop.addActionListener(listener -> {
            if (future != null) future.cancel(true);
            PartiesLedge partiesLedge = leagueClientUI.getLeagueClient().getLedge().getParties();
            PartiesRegistration registration = partiesLedge.getCurrentRegistration();
            try {
                if (registration == null) return;
                partiesLedge.setQueueAction(PartyAction.STOP);
                leagueClientUI.getChatSidebar().getEssentials().disableQueueState();
            } catch (IOException e) {
                Logger.error(e);
            }
        });
            bottom.add(stop);
            add(bottom, BorderLayout.SOUTH);
    }
}
