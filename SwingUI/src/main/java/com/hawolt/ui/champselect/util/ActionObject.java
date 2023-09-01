package com.hawolt.ui.champselect.util;

import org.json.JSONObject;

/**
 * Created: 29/08/2023 19:12
 * Author: Twitter @hawolt
 **/

public class ActionObject {
    private final int actorCellId, championId, actionId;
    private final boolean completed;
    private final String type;

    public ActionObject(JSONObject object) {
        this.actorCellId = object.getInt("actorCellId");
        this.completed = object.getBoolean("completed");
        this.championId = object.getInt("championId");
        this.actionId = object.getInt("actionId");
        this.type = object.getString("type");
    }

    public int getActorCellId() {
        return actorCellId;
    }

    public int getChampionId() {
        return championId;
    }

    public int getActionId() {
        return actionId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ActionObject{" +
                "actorCellId=" + actorCellId +
                ", championId=" + championId +
                ", actionId=" + actionId +
                ", completed=" + completed +
                ", type='" + type + '\'' +
                '}';
    }
}
