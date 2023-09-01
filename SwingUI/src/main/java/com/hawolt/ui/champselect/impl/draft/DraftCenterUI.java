package com.hawolt.ui.champselect.impl.draft;

import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.champselect.generic.impl.ChampSelectChoice;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSelectionUI;
import com.hawolt.ui.champselect.util.ActionObject;
import com.hawolt.ui.champselect.util.ChampSelectPhase;
import com.hawolt.util.panel.ChildUIComponent;

import java.awt.*;
import java.util.Optional;

/**
 * Created: 29/08/2023 18:57
 * Author: Twitter @hawolt
 **/

public class DraftCenterUI extends ChampSelectUIComponent {
    private final CardLayout layout = new CardLayout();
    private final ChampSelectSelectionUI pick, ban;
    private final ChildUIComponent main, child, cards;
    private ChampSelectPhase current;

    public DraftCenterUI(ChampSelectChoice callback) {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.ORANGE);
        this.add(main = new ChildUIComponent(new BorderLayout()), BorderLayout.CENTER);
        this.main.add(cards = new ChildUIComponent(layout), BorderLayout.CENTER);
        this.main.add(child = new ChildUIComponent(new BorderLayout()), BorderLayout.SOUTH);
        this.cards.add("select", pick = new ChampSelectSelectionUI(ChampSelectType.PICK, callback));
        this.cards.add("ban", ban = new ChampSelectSelectionUI(ChampSelectType.BAN, callback));
    }

    public ChampSelectSelectionUI getPick() {
        return pick;
    }

    public ChampSelectSelectionUI getBan() {
        return ban;
    }

    public ChampSelectPhase getCurrent() {
        return current;
    }

    public ChildUIComponent getMain() {
        return main;
    }

    public ChildUIComponent getChild() {
        return child;
    }

    @Override
    public void update() {
        int currentActionSetIndex = index.getCurrentActionSetIndex();
        Optional<ActionObject> optional = index.getOwnBanPhase();
        boolean isBanComplete = optional.isPresent() && optional.get().isCompleted();
        ChampSelectPhase phase;
        if (currentActionSetIndex < 0) {
            phase = ChampSelectPhase.PLAN;
        } else if (currentActionSetIndex == 0) {
            if (!isBanComplete) {
                phase = ChampSelectPhase.BAN;
            } else {
                phase = ChampSelectPhase.PLAN;
            }
        } else {
            phase = ChampSelectPhase.PICK;
        }
        if (phase == current) return;
        layout.show(cards, (current = phase).getName());
    }
}
