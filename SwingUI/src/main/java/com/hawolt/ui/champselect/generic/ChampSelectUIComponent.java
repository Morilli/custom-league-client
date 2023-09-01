package com.hawolt.ui.champselect.generic;

import com.hawolt.ui.champselect.ChampSelectIndex;
import com.hawolt.util.panel.ChildUIComponent;

/**
 * Created: 29/08/2023 17:13
 * Author: Twitter @hawolt
 **/

public abstract class ChampSelectUIComponent extends ChildUIComponent {
    protected ChampSelectIndex index;

    public void configure(ChampSelectIndex index) {
        this.index = index;
        if (index.getCounter() == 2) {
            this.init();
        }
        this.update();
    }

    public void update() {

    }

    public void init() {

    }
}
