package com.hawolt.ui.champselect.impl.draft;

import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.generic.impl.ChampSelectBanElement;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSidebarUI;
import com.hawolt.ui.champselect.util.ActionObject;
import com.hawolt.util.panel.ChildUIComponent;

import java.awt.*;
import java.util.List;

/**
 * Created: 31/08/2023 21:10
 * Author: Twitter @hawolt
 **/

public class DraftSelectSidebarUI extends ChampSelectSidebarUI {

    private final ChampSelectBanElement[] elements = new ChampSelectBanElement[5];

    public DraftSelectSidebarUI(ChampSelectTeam team) {
        super(team);
        ChildUIComponent bans = new ChildUIComponent(new GridLayout(0, 5, 5, 0));
        bans.setPreferredSize(new Dimension(0, 60));
        for (int i = 0; i < elements.length; i++) {
            ChampSelectBanElement element = new ChampSelectBanElement(new Dimension(48, 48));
            element.update(-1);
            elements[i] = element;
            bans.add(element);
        }
        this.main.add(bans, BorderLayout.NORTH);
    }

    @Override
    public void init() {
        if (index == null) return;
        super.init();
        for (ChampSelectBanElement element : elements) {
            element.update(-1);
        }
    }

    @Override
    public void update() {
        if (index == null) return;
        super.update();
        List<ActionObject> list = index.getBanSelection(type);
        for (ActionObject object : list) {
            int normalizedActorCellId = object.getActorCellId() % 5;
            elements[normalizedActorCellId].update(object);
        }
    }
}
