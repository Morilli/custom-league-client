package com.hawolt.ui.champselect.impl.draft;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheType;
import com.hawolt.client.resources.communitydragon.spell.Spell;
import com.hawolt.client.resources.ledge.teambuilder.objects.MatchContext;
import com.hawolt.logger.Logger;
import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.service.impl.TeamBuilderService;
import com.hawolt.rtmp.utility.PacketCallback;
import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.generic.impl.ChampSelectChatUI;
import com.hawolt.ui.champselect.generic.impl.ChampSelectDebugUI;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSelectionElement;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSidebarUI;
import com.hawolt.ui.champselect.util.ActionObject;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.objects.conversation.history.impl.IncomingMessage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

/**
 * Created: 29/08/2023 17:04
 * Author: Twitter @hawolt
 **/

public class DraftChampSelectUI extends AbstractRenderInstance implements ActionListener {
    public static DraftChampSelectUI INSTANCE = new DraftChampSelectUI();
    private final ChampSelectSidebarUI teamOne, teamTwo;
    private final DraftChampSelectHeaderUI headerUI;
    private final DraftGameSettingUI settingUI;
    private final ChampSelectChatUI chatUI;
    private final DraftCenterUI centerUI;

    private int selectedChampionId, bannedChampionId;

    public DraftChampSelectUI() {
        this.component.add(centerUI = new DraftCenterUI(this), BorderLayout.CENTER);
        this.centerUI.add(teamTwo = new DraftSelectSidebarUI(ChampSelectTeam.PURPLE), BorderLayout.EAST);
        this.centerUI.add(teamOne = new DraftSelectSidebarUI(ChampSelectTeam.BLUE), BorderLayout.WEST);
        this.centerUI.getMain().add(headerUI = new DraftChampSelectHeaderUI(), BorderLayout.NORTH);
        ChildUIComponent component = new ChildUIComponent(new BorderLayout());
        this.centerUI.getChild().add(component, BorderLayout.NORTH);
        component.add(new ChampSelectDebugUI(), BorderLayout.NORTH);
        component.add(settingUI = new DraftGameSettingUI(), BorderLayout.CENTER);
        this.centerUI.getChild().add(chatUI = new ChampSelectChatUI(), BorderLayout.CENTER);
        this.build();
    }

    @Override
    protected void push(IncomingMessage incomingMessage) {
        if (chatUI == null) return;
        chatUI.push(incomingMessage);
    }

    @Override
    public void init() {
        super.init();
        //JOIN CHATROOM WHEN CHAMP SELECT STARTS
        LeagueClientUI.service.execute(() -> {
            LeagueClient client = index.getLeagueClient();
            if (client == null) return;
            MatchContext context = client.getCachedValue(CacheType.MATCH_CONTEXT);
            if (context == null) return;
            chatUI.setMatchContext(context);
            VirtualRiotXMPPClient xmppClient = client.getXMPPClient();
            xmppClient.joinUnprotectedMuc(context.getPayload().getChatRoomName(), context.getPayload().getTargetRegion());
        });
    }

    private void build() {
        settingUI.getSubmitButton().addActionListener(this);
        settingUI.getRuneButton().addActionListener(this);
        settingUI.getSpellOne().addActionListener(this);
        settingUI.getSpellTwo().addActionListener(this);
    }

    public ChampSelectSidebarUI getTeamOne() {
        return teamOne;
    }

    public ChampSelectSidebarUI getTeamTwo() {
        return teamTwo;
    }

    @Override
    public int[] getSupportedQueueIds() {
        return new int[]{400, 420, 440};
    }

    @Override
    public String getCardName() {
        return "draft";
    }

    @Override
    public void onSummonerSubmission(Spell selectedSpellOne, Spell selectedSpellTwo) {
        try {
            Logger.info("{}, {}, {}, {}", selectedSpellOne.getName(), selectedSpellOne.getId(), selectedSpellTwo.getName(), selectedSpellTwo.getId());
            if (index == null) {
                Logger.info("RETURN 1");
                return;
            }
            LeagueRtmpClient rtmpClient = index.getLeagueClient().getRTMPClient();
            TeamBuilderService teamBuilderService = rtmpClient.getTeamBuilderService();
            teamBuilderService.selectSpellsBlocking(selectedSpellOne.getId(), selectedSpellTwo.getId());
        } catch (Exception e) {
            Logger.error("Unable to submit selection");
            Logger.error(e);
        }
    }

    protected void onChoice(ActionObject actionObject, int championId, boolean completed) {
        if (index == null) return;
        try {
            LeagueRtmpClient rtmpClient = index.getLeagueClient().getRTMPClient();
            TeamBuilderService teamBuilderService = rtmpClient.getTeamBuilderService();
            teamBuilderService.updateActionV1Asynchronous(new PacketCallback() {
                @Override
                public void onPacket(RtmpPacket rtmpPacket, TypedObject typedObject) {
                    Logger.info("OBJECT: {}", typedObject);
                }
            }, actionObject.getActionId(), championId, completed);
        } catch (Exception e) {
            Logger.error("Unable to submit selection");
            Logger.error(e);
        }
    }

    @Override
    public void onChoiceSubmission(ChampSelectType type, int championId, boolean completed) {
        if (index == null) {
            Logger.info("RETURN 2");
            return;
        }
        Optional<ActionObject> optional = switch (type) {
            case PICK -> index.getOwnPickPhase();
            case BAN -> index.getOwnBanPhase();
        };
        optional.ifPresent(phase -> {
            Logger.info("{}, {}, {}, {}", type, phase.getActionId(), championId, completed);
            onChoice(phase, championId, completed);
        });
    }

    @Override
    public void onChoice(ChampSelectSelectionElement element) {
        super.onChoice(element);
        int championId = element.getChampionId();
        switch (element.getType()) {
            case PICK -> this.selectedChampionId = championId;
            case BAN -> this.bannedChampionId = championId;
        }
        this.onChoiceSubmission(element.getType(), championId, false);
    }

    @Override
    public void invokeChampionFilter(String champion) {
        this.centerUI.getPick().filter(champion);
        this.centerUI.getBan().filter(champion);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Submit Choice" -> {
                if (index.getCurrentActionSetIndex() == 0) {
                    onChoiceSubmission(ChampSelectType.BAN, bannedChampionId, true);
                } else {
                    onChoiceSubmission(ChampSelectType.PICK, selectedChampionId, true);
                }
            }
            case "Configure Runes" -> {
                Logger.info("Should switch to runes panel");
            }
            case "comboBoxChanged" -> {
                onSummonerSubmission(
                        settingUI.getSelectedSpellOne(),
                        settingUI.getSelectedSpellTwo()
                );
            }
        }
    }
}
