package com.hawolt.ui.champselect.util;

import org.json.JSONObject;

/**
 * Created: 30/08/2023 16:33
 * Author: Twitter @hawolt
 **/

public class ChampSelectMember {
    protected final String nameVisibilityType, summonerName;
    protected final int teamId, cellId, championId;
    protected final long summonerId;

    public ChampSelectMember(JSONObject object) {
        this.nameVisibilityType = object.getString("nameVisibilityType");
        this.summonerName = object.getString("summonerName");
        this.summonerId = object.getLong("summonerId");
        this.championId = object.getInt("championId");
        this.teamId = object.getInt("teamId");
        this.cellId = object.getInt("cellId");
    }

    public long getSummonerId() {
        return summonerId;
    }

    public int getTeamId() {
        return teamId;
    }

    public int getCellId() {
        return cellId;
    }

    public int getChampionId() {
        return championId;
    }

    public String getNameVisibilityType() {
        return nameVisibilityType;
    }

    public String getSummonerName() {
        return summonerName;
    }

    @Override
    public String toString() {
        return "ChampSelectMember{" +
                "nameVisibilityType='" + nameVisibilityType + '\'' +
                ", summonerName='" + summonerName + '\'' +
                ", teamId=" + teamId +
                ", cellId=" + cellId +
                ", championId=" + championId +
                ", summonerId=" + summonerId +
                '}';
    }
}
