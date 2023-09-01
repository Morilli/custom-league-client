package com.hawolt.ui.champselect;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheType;
import com.hawolt.logger.Logger;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.utility.Base64GZIP;
import com.hawolt.rtmp.utility.PacketCallback;
import com.hawolt.ui.champselect.data.ChampSelectTeamType;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.champselect.impl.draft.DraftChampSelectUI;
import com.hawolt.ui.champselect.util.*;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.xmpp.event.handler.message.IMessageListener;
import com.hawolt.xmpp.event.objects.conversation.history.impl.FailedMessage;
import com.hawolt.xmpp.event.objects.conversation.history.impl.IncomingMessage;
import com.hawolt.xmpp.event.objects.conversation.history.impl.OutgoingMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created: 29/08/2023 16:59
 * Author: Twitter @hawolt
 **/

public class ChampSelectUI extends ChildUIComponent implements ChampSelectIndex, PacketCallback, IMessageListener {

    private final Map<Integer, String> QUEUE_RENDERER_MAPPING = new HashMap<>();
    private final Set<AbstractRenderInstance> instances = new HashSet<>();
    private final Map<String, String> resolver = new HashMap<>();
    private final CardLayout layout = new CardLayout();
    private final JComponent main = new ChildUIComponent(layout);


    protected boolean allowDuplicatePicks, skipChampionSelect, allowSkinSelection, allowOptingOutOfBanning;
    protected int localPlayerCellId, currentActionSetIndex, counter, recoveryCounter, queueId;
    protected Map<Integer, List<ActionObject>> actionSetMapping = new ConcurrentHashMap<>();
    protected String teamId, subphase, teamChatRoomId, phaseName, contextId, filter;
    protected long currentTotalTimeMillis, currentTimeRemainingMillis, gameId;
    protected int[] championsAvailableForBan;
    protected LeagueClient leagueClient;
    protected JSONObject cells;

    public ChampSelectUI(LeagueClient leagueClient) {
        super(new BorderLayout());
        this.leagueClient = leagueClient;
        this.add(main, BorderLayout.CENTER);
        this.addRenderInstance(DraftChampSelectUI.INSTANCE);
        this.addRenderInstance(AbstractRenderInstance.INSTANCE);
        this.main.add("blank", new ChildUIComponent(null));
        this.layout.show(main, "blank");
        if (leagueClient == null) return;
        leagueClient.getRTMPClient().setDefaultCallback(this);
    }

    public ChampSelectUI() {
        this(null);
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
                configure(new JSONObject(Base64GZIP.unzipBase64(object.toString())));
            } catch (IOException e) {
                Logger.error(e);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void addRenderInstance(AbstractRenderInstance instance) {
        int[] queueIds = instance.getSupportedQueueIds();
        for (int id : queueIds) {
            QUEUE_RENDERER_MAPPING.put(id, instance.getCardName());
        }
        this.instances.add(instance);
        this.main.add(instance.getCardName(), instance);
    }

    public void configure(JSONObject object) {
        Logger.info(object);
        this.gameId = object.getLong("gameId");
        this.queueId = object.getInt("queueId");
        this.counter = object.getInt("counter");
        this.phaseName = object.getString("phaseName");
        this.contextId = object.getString("contextId");
        this.recoveryCounter = object.getInt("recoveryCounter");
        JSONObject championSelectState = object.getJSONObject("championSelectState");
        this.currentTimeRemainingMillis = championSelectState.getLong("currentTimeRemainingMillis");
        this.allowOptingOutOfBanning = championSelectState.getBoolean("allowOptingOutOfBanning");
        this.currentTotalTimeMillis = championSelectState.getLong("currentTotalTimeMillis");
        this.currentActionSetIndex = championSelectState.getInt("currentActionSetIndex");
        this.allowDuplicatePicks = championSelectState.getBoolean("allowDuplicatePicks");
        this.allowSkinSelection = championSelectState.getBoolean("allowSkinSelection");
        this.skipChampionSelect = championSelectState.getBoolean("skipChampionSelect");
        this.localPlayerCellId = championSelectState.getInt("localPlayerCellId");
        this.teamChatRoomId = championSelectState.getString("teamChatRoomId");
        this.subphase = championSelectState.getString("subphase");
        this.cells = championSelectState.getJSONObject("cells");
        this.teamId = championSelectState.getString("teamId");
        JSONArray actionSetList = championSelectState.getJSONArray("actionSetList");
        for (int i = 0; i < actionSetList.length(); i++) {
            JSONArray actionSetListChild = actionSetList.getJSONArray(i);
            List<ActionObject> list = new ArrayList<>();
            for (int j = 0; j < actionSetListChild.length(); j++) {
                ActionObject actionObject = new ActionObject(actionSetListChild.getJSONObject(j));
                list.add(actionObject);
            }
            actionSetMapping.put(i, list);
        }
        JSONObject inventoryDraft = championSelectState.getJSONObject("inventoryDraft");
        List<String> disabledChampionIds = inventoryDraft.getJSONArray("disabledChampionIds")
                .toList()
                .stream()
                .map(Object::toString)
                .toList();
        championsAvailableForBan = inventoryDraft.getJSONArray("allChampionIds")
                .toList()
                .stream()
                .map(Object::toString)
                .filter(o -> !disabledChampionIds.contains(o))
                .mapToInt(Integer::parseInt)
                .toArray();
        this.update(this);
    }

    private void update(ChampSelectIndex index) {
        if (index.getCounter() == 2) {
            String card = QUEUE_RENDERER_MAPPING.getOrDefault(index.getQueueId(), "blank");
            Logger.debug("[champ-select] switch to card {}", card);
            layout.show(main, card);
        }
        this.configure(main, index);
        this.repaint();
    }

    private void configure(JComponent main, ChampSelectIndex index) {
        Component[] components = main.getComponents();
        for (Component component : components) {
            if (component == null) continue;
            if (!(component instanceof JComponent child)) continue;
            if ((child instanceof ChampSelectUIComponent champSelectUIComponent)) {
                champSelectUIComponent.configure(index);
            }
            configure(child, index);
        }
    }

    @Override
    public Optional<ActionObject> getOwnBanPhase() {
        return actionSetMapping.get(0)
                .stream()
                .filter(object -> object.getActorCellId() == localPlayerCellId)
                .findFirst();
    }

    @Override
    public Optional<ActionObject> getOwnPickPhase() {
        return actionSetMapping.values()
                .stream()
                .skip(1)
                .flatMap(Collection::stream)
                .filter(object -> object.getActorCellId() == localPlayerCellId)
                .findFirst();
    }

    @Override
    public List<ActionObject> getBanSelection(ChampSelectTeamType type) {
        List<Integer> cellIds = Arrays.stream(getCells(type, MemberFunction.INSTANCE))
                .map(ChampSelectMember::getCellId)
                .toList();
        return actionSetMapping.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(actionObject -> actionObject.getType().equals("BAN"))
                .filter(actionObject -> cellIds.contains(actionObject.getActorCellId()))
                .collect(Collectors.toList());
    }

    @Override
    public int[] getChampionsAvailableForBan() {
        return championsAvailableForBan;
    }

    @Override
    public int[] getChampionsAvailableForPick() {
        if (leagueClient == null) return championsAvailableForBan;
        String jwt = leagueClient.getCachedValue(CacheType.INVENTORY_TOKEN);
        JSONObject b = new JSONObject(new String(Base64.getDecoder().decode(jwt.split("\\.")[1])));
        JSONObject items = b.getJSONObject("items");
        JSONArray champions = items.getJSONArray("CHAMPION");
        int[] ids = new int[champions.length()];
        for (int i = 0; i < champions.length(); i++) {
            ids[i] = champions.getInt(i);
        }
        return ids;
    }

    @Override
    public ChampSelectTeamMember getSelf() {
        ChampSelectMember[] members = getCells(ChampSelectTeamType.ALLIED, TeamMemberFunction.INSTANCE);
        for (ChampSelectMember member : members) {
            if (member.getCellId() == getLocalPlayerCellId()) {
                return (ChampSelectTeamMember) member;
            }
        }
        return null;
    }

    @Override
    public Set<String> getCells() {
        return cells.keySet();
    }

    @Override
    public LeagueClient getLeagueClient() {
        return leagueClient;
    }

    @Override
    public JSONArray getCells(ChampSelectTeamType type) {
        return cells.getJSONArray(type.getIdentifier());
    }

    @Override
    public <T> T getCells(ChampSelectTeamType type, Function<JSONArray, T> function) {
        return function.apply(getCells(type));
    }

    @Override
    public void cache(String puuid, String name) {
        this.resolver.put(puuid, name);
    }

    @Override
    public Map<String, String> getPUUIDResolver() {
        return resolver;
    }

    @Override
    public Map<Integer, List<ActionObject>> getActionSetMapping() {
        return actionSetMapping;
    }

    @Override
    public boolean isAllowDuplicatePicks() {
        return allowDuplicatePicks;
    }

    @Override
    public boolean isSkipChampionSelect() {
        return skipChampionSelect;
    }

    @Override
    public boolean isAllowSkinSelection() {
        return allowSkinSelection;
    }

    @Override
    public boolean isAllowOptingOutOfBanning() {
        return allowOptingOutOfBanning;
    }

    @Override
    public long getCurrentTotalTimeMillis() {
        return currentTotalTimeMillis;
    }

    @Override
    public long getCurrentTimeRemainingMillis() {
        return currentTimeRemainingMillis;
    }

    @Override
    public int getLocalPlayerCellId() {
        return localPlayerCellId;
    }

    @Override
    public int getCurrentActionSetIndex() {
        return currentActionSetIndex;
    }

    @Override
    public String getTeamId() {
        return teamId;
    }

    @Override
    public String getSubphase() {
        return subphase;
    }

    @Override
    public String getTeamChatRoomId() {
        return teamChatRoomId;
    }

    @Override
    public int getCounter() {
        return counter;
    }

    @Override
    public int getRecoveryCounter() {
        return recoveryCounter;
    }

    @Override
    public int getQueueId() {
        return queueId;
    }

    @Override
    public long getGameId() {
        return gameId;
    }

    @Override
    public String getPhaseName() {
        return phaseName;
    }

    @Override
    public String getContextId() {
        return contextId;
    }

    @Override
    public boolean isFinalizing() {
        int availableActions = getActionSetMapping().size();
        return getCurrentActionSetIndex() >= availableActions;
    }

    @Override
    public List<ActionObject> getCurrent() {
        if (getCurrentActionSetIndex() < 0) return new ArrayList<>();
        return getActionSetMapping().get(getCurrentActionSetIndex());
    }

    @Override
    public void filterChampion(String champion) {
        for (AbstractRenderInstance instance : instances) {
            instance.invokeChampionFilter(champion);
        }
    }


    @Override
    public void onMessageReceived(IncomingMessage incomingMessage) {
        Logger.error(incomingMessage);
        if (!incomingMessage.getType().equals("groupchat")) return;
        for (AbstractRenderInstance instance : instances) {
            instance.push(incomingMessage);
        }
    }

    @Override
    public void onMessageSent(OutgoingMessage outgoingMessage) {

    }

    @Override
    public void onFailedMessage(FailedMessage failedMessage) {

    }
}
