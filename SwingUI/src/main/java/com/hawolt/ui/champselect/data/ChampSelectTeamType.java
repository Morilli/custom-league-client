package com.hawolt.ui.champselect.data;

/**
 * Created: 30/08/2023 16:32
 * Author: Twitter @hawolt
 **/

public enum ChampSelectTeamType {
    ALLIED("alliedTeam"), ENEMY("enemyTeam");
    final String identifier;

    ChampSelectTeamType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
