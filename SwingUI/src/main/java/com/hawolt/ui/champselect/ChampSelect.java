package com.hawolt.ui.champselect;

import com.hawolt.LeagueClientUI;
import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheType;
import com.hawolt.logger.Logger;
import com.hawolt.objects.Champion;
import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.utility.Base64GZIP;
import com.hawolt.rtmp.utility.PacketCallback;
import com.hawolt.ui.champselect.header.ChampSelectHeaderUI;
import com.hawolt.ui.champselect.phase.ChampSelectPhaseUI;
import com.hawolt.ui.champselect.sidebar.ChampSelectSidebarUI;
import com.hawolt.util.AudioEngine;
import com.hawolt.util.panel.ChildUIComponent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 06/08/2023 13:39
 * Author: Twitter @hawolt
 **/

public class ChampSelect extends ChildUIComponent implements PacketCallback, IChampSelection, ActionListener, ISpellChangedListener, ResourceConsumer<JSONArray, byte[]> {

    private final ChampSelectSidebarUI teamOneUI, teamTwoUI;
    private final ChampSelectHeaderUI headerUI;
    private final ChampSelectPhaseUI phaseUI;

    private LeagueClientUI leagueClientUI;
    private LeagueRtmpClient rtmpClient;
    private LeagueClient leagueClient;

    public ChampSelect() {
        super(new BorderLayout());
        this.add(phaseUI = new ChampSelectPhaseUI(null, this), BorderLayout.CENTER);
        this.add(headerUI = new ChampSelectHeaderUI(), BorderLayout.NORTH);
        this.add(teamOneUI = new ChampSelectSidebarUI(), BorderLayout.WEST);
        this.add(teamTwoUI = new ChampSelectSidebarUI(), BorderLayout.EAST);
        this.phaseUI.configure(this);
        ResourceLoader.loadResource("https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/champion-summary.json", this);
    }

    public ChampSelect(LeagueClientUI leagueClientUI) {
        super(new BorderLayout());
        this.leagueClientUI = leagueClientUI;
        this.leagueClient = leagueClientUI.getLeagueClient();
        this.add(phaseUI = new ChampSelectPhaseUI(this, this), BorderLayout.CENTER);
        this.add(headerUI = new ChampSelectHeaderUI(), BorderLayout.NORTH);
        this.add(teamOneUI = new ChampSelectSidebarUI(), BorderLayout.WEST);
        this.add(teamTwoUI = new ChampSelectSidebarUI(), BorderLayout.EAST);
        this.phaseUI.getPickPhaseUI().getButton().addActionListener(this);
        this.phaseUI.getBanPhaseUI().getButton().addActionListener(this);
        this.phaseUI.configure(this);
        this.rtmpClient = leagueClient.getRTMPClient();
        this.rtmpClient.setDefaultCallback(this);
        ResourceLoader.loadResource("https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/champion-summary.json", this);
    }

    public LeagueClient getLeagueClient() {
        return leagueClient;
    }

    private Map<String, Integer> actions = new HashMap<>();
    private int currentActionSetIndex, localPlayerCellId, ownTeamId;

    private ChampSelectSidebarUI getOwnSidebarUI() {
        return ownTeamId == 1 ? teamOneUI : teamTwoUI;
    }

    public String getHiddenName(String puuid) {
        ChampSelectSidebarUI sidebarUI = getOwnSidebarUI();
        if (sidebarUI == null) return null;
        return getOwnSidebarUI().find(puuid);
    }

    public void populate(JSONObject object) {
        AudioEngine.play("ChmpSlct_AChampionApproaches.wav");

        this.phaseUI.getChatUI().build();
        this.phaseUI.show("pick");
        this.resetChampSelectState();

        JSONObject state = object.getJSONObject("championSelectState");
        this.currentActionSetIndex = state.getInt("currentActionSetIndex");

        this.localPlayerCellId = state.getInt("localPlayerCellId");
        JSONArray array = state.getJSONArray("actionSetList");
        for (int i = 0; i < array.length(); i++) {
            JSONArray nested = array.getJSONArray(i);
            for (int j = 0; j < nested.length(); j++) {
                JSONObject action = nested.getJSONObject(j);
                int actorCellId = action.getInt("actorCellId");
                if (actorCellId != localPlayerCellId) continue;
                String type = action.getString("type");
                int actionId = action.getInt("actionId");
                actions.put(type, actionId);
            }
        }

        JSONObject inventoryDraft = state.getJSONObject("inventoryDraft");
        JSONArray allChampionIds = inventoryDraft.getJSONArray("allChampionIds");
        int currentActionSetIndex = state.getInt("currentActionSetIndex");

        headerUI.getTimerUI().update(currentActionSetIndex, "");
        phaseUI.getBanPhaseUI().getSelectionUI().update(this, allChampionIds);

        JSONObject cells = state.getJSONObject("cells");
        JSONArray allied = cells.getJSONArray("alliedTeam");
        JSONArray enemy = cells.getJSONArray("enemyTeam");
        this.ownTeamId = allied.getJSONObject(0).getInt("teamId");

        JSONArray teamOne = ownTeamId == 1 ? allied : enemy;
        JSONArray teamTwo = ownTeamId == 1 ? enemy : allied;

        headerUI.getTeamTwoUI().rebuild(teamTwo);
        teamOneUI.rebuild(this, teamOne, localPlayerCellId);
        headerUI.getTeamOneUI().rebuild(teamOne);
        teamTwoUI.rebuild(this, teamTwo, localPlayerCellId);
    }

    public void update(String data) {
        try {
            //JUST LOG, GOD HAVE MERCY ON US
            Logger.info(data);

            JSONObject object = new JSONObject(data);
            if (object.getInt("counter") == 2) populate(object);
            JSONObject state = object.getJSONObject("championSelectState");
            JSONArray array = state.getJSONArray("actionSetList");
            //DISPLAY BANS
            headerUI.update(array.getJSONArray(0));

            String subphase = state.getString("subphase");
            int currentActionSetIndex = state.getInt("currentActionSetIndex");
            Logger.info("{}:{}", currentActionSetIndex, subphase);
            //DISPLAY CORRECT PHASE
            if (currentActionSetIndex == 0 && !phaseUI.getCurrent().equals("ban")) {
                phaseUI.show("ban");
            } else if (currentActionSetIndex > 0 && !phaseUI.getCurrent().equals("pick")) {
                phaseUI.show("pick");
            }
            //UPDATE HEADERS
            long currentTotalTimeMillis = state.getLong("currentTotalTimeMillis");
            long currentTimeRemainingMillis = state.getLong("currentTimeRemainingMillis");
            headerUI.getTimerUI().update(currentTotalTimeMillis, currentTimeRemainingMillis);
            headerUI.getTimerUI().update(currentActionSetIndex, subphase);
            for (int i = 1; i < array.length(); i++) {
                JSONArray phase = array.getJSONArray(i);
                teamOneUI.update(currentActionSetIndex, i, phase);
                teamTwoUI.update(currentActionSetIndex, i, phase);
            }
            //UPDATE OWN TEAM
            JSONObject cells = state.getJSONObject("cells");
            JSONArray allied = cells.getJSONArray("alliedTeam");
            for (int i = 0; i < allied.length(); i++) {
                JSONObject member = allied.getJSONObject(i);
                ChampSelectSidebarUI champSelectSidebarUI = ownTeamId == 1 ? teamOneUI : teamTwoUI;
                champSelectSidebarUI.update(new AlliedMember(member));
            }
            //UPDATE ENEMY TEAM
            JSONArray enemy = cells.getJSONArray("enemyTeam");
            for (int i = 0; i < enemy.length(); i++) {
                JSONObject member = enemy.getJSONObject(i);
                ChampSelectSidebarUI champSelectSidebarUI = ownTeamId == 2 ? teamOneUI : teamTwoUI;
                champSelectSidebarUI.update(member);
            }
            //RELOAD
            this.revalidate();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public void reconfigure() {
        if (leagueClient == null) return;
        String jwt = leagueClient.getCachedValue(CacheType.INVENTORY_TOKEN);
        JSONObject b = new JSONObject(new String(Base64.getDecoder().decode(jwt.split("\\.")[1])));
        JSONObject items = b.getJSONObject("items");
        JSONArray champions = items.getJSONArray("CHAMPION");
        Integer[] ids = new Integer[champions.length()];
        for (int i = 0; i < champions.length(); i++) {
            ids[i] = champions.getInt(i);
        }
        phaseUI.getPickPhaseUI().getSelectionUI().update(this, ids);
    }

    public void resetChampSelectState() {
        this.teamOneUI.reset();
        this.teamTwoUI.reset();
        this.headerUI.reset();
        this.reconfigure();
        this.revalidate();
    }

    @Override
    public void onSelect(ChampSelectPhase phase, long championId) {
        Logger.info("HOVER {}:{}:{}", phase, championId, actions.getOrDefault(phase.name(), -1));
        LeagueClientUI.service.execute(() -> {
            try {
                TypedObject object = rtmpClient.getTeamBuilderService().updateActionV1Blocking(actions.get(phase.name()), (int) championId, false);
                Logger.error(object);
            } catch (Exception e) {
                Logger.error(e);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String command = e.getActionCommand();
        LeagueClientUI.service.execute(() -> {
            try {
                switch (command) {
                    case "DODGE" -> {
                        this.rtmpClient.getTeamBuilderService().quitGameV2Asynchronous(this);
                        this.leagueClientUI.getChatSidebar().getEssentials().disableQueueState();
                        this.headerUI.getTimerUI().stop();
                        this.phaseUI.getChatUI().reset();
                        this.resetChampSelectState();
                        this.revalidate();
                    }
                    case "BAN" -> {
                        int championId = (int) this.phaseUI.getBanPhaseUI().getSelectionUI().getSelectedChampionId();
                        this.rtmpClient.getTeamBuilderService().updateActionV1Blocking(
                                actions.get(command),
                                championId,
                                true
                        );
                        this.phaseUI.show("pick");
                    }
                    case "PICK" -> {
                        int championId = (int) this.phaseUI.getPickPhaseUI().getSelectionUI().getSelectedChampionId();
                        this.rtmpClient.getTeamBuilderService().updateActionV1Blocking(
                                actions.get(command),
                                championId,
                                true
                        );
                    }
                }
            } catch (Exception exception) {
                Logger.error(exception);
            }
        });
    }

    @Override
    public void onPacket(RtmpPacket rtmpPacket, TypedObject typedObject) {
        try {
            if (typedObject == null || !typedObject.containsKey("data")) return;
            TypedObject data = typedObject.getTypedObject("data");
            if (data == null || !data.containsKey("flex.messaging.messages.AsyncMessage")) return;
            TypedObject message = data.getTypedObject("flex.messaging.messages.AsyncMessage");
            if (message == null || !message.containsKey("body")) return;
            TypedObject body = message.getTypedObject("body");
            if (body == null || !body.containsKey("com.riotgames.platform.serviceproxy.dispatch.LcdsServiceProxyResponse"))
                return;
            TypedObject response = body.getTypedObject("com.riotgames.platform.serviceproxy.dispatch.LcdsServiceProxyResponse");
            if (response == null || !response.containsKey("payload")) return;
            try {
                Object object = response.get("payload");
                if (object == null) return;
                update(Base64GZIP.unzipBase64(object.toString()));
            } catch (IOException e) {
                Logger.error(e);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    @Override
    public void onSpellSelection(int spell1Id, int spell2Id) {
        LeagueClientUI.service.execute(() -> {
            try {
                rtmpClient.getTeamBuilderService().selectSpellsAsynchronous(spell1Id, spell2Id, this);
            } catch (IOException e) {
                Logger.error(e);
            }
        });
    }

    private final Map<Integer, Champion> cache = new HashMap<>();


    @Override
    public Map<Integer, Champion> getChampionCache() {
        return cache;
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.fatal("Failed to load '{}'", o);
        Logger.error(e);
    }

    @Override
    public void consume(Object o, JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject reference = array.getJSONObject(i);
            Champion champion = new Champion(reference);
            cache.put(champion.getId(), champion);
        }
        reconfigure();
        revalidate();
    }

    @Override
    public JSONArray transform(byte[] bytes) throws Exception {
        return new JSONArray(new String(bytes));
    }
}
