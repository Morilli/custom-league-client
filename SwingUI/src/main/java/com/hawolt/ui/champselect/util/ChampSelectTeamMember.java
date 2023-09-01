package com.hawolt.ui.champselect.util;

import org.json.JSONObject;

/**
 * Created: 30/08/2023 16:33
 * Author: Twitter @hawolt
 **/

public class ChampSelectTeamMember extends ChampSelectMember {

    private final String puuid, assignedPosition, entitledFeatureType;
    private final int championPickIntent, spell1Id, spell2Id, skinId;

    public ChampSelectTeamMember(JSONObject object) {
        super(object);
        this.entitledFeatureType = object.getString("entitledFeatureType");
        this.championPickIntent = object.getInt("championPickIntent");
        this.assignedPosition = object.getString("assignedPosition");
        this.spell1Id = object.getInt("spell1Id");
        this.spell2Id = object.getInt("spell2Id");
        this.puuid = object.getString("puuid");
        this.skinId = object.getInt("skinId");
    }

    public String getPUUID() {
        return puuid;
    }

    public String getAssignedPosition() {
        return assignedPosition;
    }

    public String getEntitledFeatureType() {
        return entitledFeatureType;
    }

    public int getChampionPickIntent() {
        return championPickIntent;
    }

    public int getSpell1Id() {
        return spell1Id;
    }

    public int getSpell2Id() {
        return spell2Id;
    }

    public int getSkinId() {
        return skinId;
    }
}
