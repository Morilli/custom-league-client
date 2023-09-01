package com.hawolt.ui.champselect.util;

/**
 * Created: 29/08/2023 19:23
 * Author: Twitter @hawolt
 **/

public enum ChampSelectPhase {
    PLAN("select"), BAN("ban"), PICK("select"), FINALIZE("finalize"), IDLE("select");
    final String name;

    ChampSelectPhase(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
