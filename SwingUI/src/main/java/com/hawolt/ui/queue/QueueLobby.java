package com.hawolt.ui.queue;

import com.hawolt.LeagueClientUI;
import com.hawolt.async.ExecutorManager;
import com.hawolt.client.resources.ledge.LedgeEndpoint;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.parties.objects.*;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyAction;
import com.hawolt.client.resources.ledge.parties.objects.data.PositionPreference;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Created: 11/08/2023 23:00
 * Author: Twitter @hawolt
 **/

public class QueueLobby extends ChildUIComponent implements ActionListener, IServiceMessageListener<RiotMessageServiceMessage> {
    private final ScheduledExecutorService scheduler = ExecutorManager.getScheduledService("queue-resumer");
    private final ChildUIComponent grid = new ChildUIComponent(new GridLayout(0, 5));
    private final JComboBox<PositionPreference> main, other;
    private final LeagueClientUI leagueClientUI;
    private ScheduledFuture<?> future;

    public QueueLobby(LeagueClientUI leagueClientUI, Container parent, CardLayout layout) {
        super(new BorderLayout());
        this.leagueClientUI = leagueClientUI;
        this.leagueClientUI.getLeagueClient().getRMSClient().getHandler().addMessageServiceListener(MessageService.PARTIES, this);
        JButton close = new JButton("Return to previous Component");
        close.addActionListener(listener -> layout.show(parent, "modes"));
        add(close, BorderLayout.NORTH);
        ChildUIComponent component = new ChildUIComponent(new BorderLayout());
        JButton invite = new JButton("Invite another Summoner");
        invite.addActionListener(listener -> {
            String name = (String) JOptionPane.showInputDialog(
                    this,
                    "Summonername",
                    "Invite",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");
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
        ChildUIComponent roles = new ChildUIComponent(new GridLayout(0, 2, 5, 0));
        main = new JComboBox<>(PositionPreference.values());
        main.addActionListener(this);
        roles.add(main);
        other = new JComboBox<>(PositionPreference.values());
        other.addActionListener(this);
        //TODO revisit
      /*  try {
            JSONObject partiesPositionPreferences = PlayerPreferencesService.get().getSettings().getPartiesPositionPreferences();
            JSONObject data = partiesPositionPreferences.getJSONObject("data");
            main.setSelectedItem(PositionPreference.valueOf(data.getString("firstPreference")));
            other.setSelectedItem(PositionPreference.valueOf(data.getString("secondPreference")));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        roles.add(other);
        component.add(roles, BorderLayout.SOUTH);
        for (int i = 0; i < 5; i++) grid.add(new SummonerComponent());
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
                partiesLedge.ready();
                PositionPreference primary = main.getItemAt(main.getSelectedIndex());
                PositionPreference secondary = other.getItemAt(other.getSelectedIndex());
                partiesLedge.metadata(primary, secondary);
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

    private SummonerComponent getSummonerComponentAt(int id) {
        int index;
        if (id == 0) index = 2;
        else if (id == 1) index = 1;
        else if (id == 2) index = 3;
        else if (id == 3) index = 0;
        else index = 4;
        return (SummonerComponent) grid.getComponent(index);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            PositionPreference primary = main.getItemAt(main.getSelectedIndex());
            PositionPreference secondary = other.getItemAt(other.getSelectedIndex());
            PartiesLedge partiesLedge = leagueClientUI.getLeagueClient().getLedge().getParties();
            partiesLedge.metadata(primary, secondary);
            //TODO revisit
            /*PlayerPreferencesLedge playerPreferencesLedge = leagueClientUI.getLeagueClient().getLedge().getPlayerPreferences();
            JSONObject playerPrefs = playerPreferencesLedge.getPlayerPreferences();
            JSONObject partiesPosPref;
            JSONObject data;
            try {
                partiesPosPref = playerPrefs.getJSONObject("partiesPositionPreferences");
                data = partiesPosPref.getJSONObject("data");
                data.remove("firstPreference");
                data.remove("secondPreference");
            } catch (Exception e2) {
                partiesPosPref = new JSONObject();
                data = new JSONObject();
                partiesPosPref.put("data", data);
                playerPrefs.put("partiesPositionPreferences", partiesPosPref);
            }
            data.put("firstPreference", main.getItemAt(main.getSelectedIndex()).toString());
            data.put("secondPreference", other.getItemAt(other.getSelectedIndex()).toString());
            playerPreferencesLedge.setPlayerPreferences(playerPrefs.toString());
            PlayerPreferencesService.get().getSettings().setPartiesPositionPreferences(partiesPosPref);
            PlayerPreferencesService.get().writeSettingsFile();*/

        } catch (IOException ex) {
            Logger.error(ex);
        }
    }
}
