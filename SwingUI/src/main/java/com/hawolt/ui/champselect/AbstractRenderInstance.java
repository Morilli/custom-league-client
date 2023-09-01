package com.hawolt.ui.champselect;

import com.hawolt.client.resources.communitydragon.spell.Spell;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.champselect.generic.impl.ChampSelectChoice;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSelectionElement;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.xmpp.event.objects.conversation.history.impl.IncomingMessage;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created: 29/08/2023 18:04
 * Author: Twitter @hawolt
 **/

public abstract class AbstractRenderInstance extends ChampSelectUIComponent implements ChampSelectRenderer, ChampSelectChoice {
    public static AbstractRenderInstance INSTANCE = new AbstractRenderInstance() {

        @Override
        protected void push(IncomingMessage incomingMessage) {

        }

        @Override
        public void invokeChampionFilter(String champion) {

        }

        @Override
        public void onSummonerSubmission(Spell selectedSpellOne, Spell selectedSpellTwo) {

        }

        @Override
        public void onChoiceSubmission(ChampSelectType type, int championId, boolean completed) {

        }

        @Override
        public int[] getSupportedQueueIds() {
            return new int[0];
        }

        @Override
        public String getCardName() {
            return "blank";
        }

        @Override
        public void update() {

        }
    };

    protected final ChildUIComponent component = new ChildUIComponent(new BorderLayout());

    public AbstractRenderInstance() {
        this.setLayout(new BorderLayout());
        this.add(component, BorderLayout.CENTER);
    }

    private final Map<ChampSelectType, ChampSelectSelectionElement> map = new ConcurrentHashMap<>();


    protected abstract void push(IncomingMessage incomingMessage);

    public abstract void invokeChampionFilter(String champion);

    @Override
    public void init() {
        for (ChampSelectType type : map.keySet()) {
            ChampSelectSelectionElement element = map.get(type);
            element.setSelected(false);
            element.repaint();
        }
        map.clear();
    }

    @Override
    public void onChoice(ChampSelectSelectionElement element) {
        if (element == null) return;
        if (map.containsKey(element.getType())) {
            ChampSelectSelectionElement champSelectSelectionElement = map.get(element.getType());
            if (champSelectSelectionElement == null) return;
            champSelectSelectionElement.setSelected(false);
            champSelectSelectionElement.repaint();
        }
        map.put(element.getType(), element);
    }
}
