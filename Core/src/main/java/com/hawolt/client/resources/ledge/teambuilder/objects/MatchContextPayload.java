package com.hawolt.client.resources.ledge.teambuilder.objects;

import org.json.JSONObject;

/**
 * Created: 14/08/2023 19:37
 * Author: Twitter @hawolt
 **/

public class MatchContextPayload {
    private final String chatRoomName, domain, targetRegion;

    public MatchContextPayload(JSONObject o) {
        JSONObject afkCheckState = o.getJSONObject("afkCheckState");
        this.chatRoomName = afkCheckState.getString("chatRoomName");
        JSONObject mucJwtDto = afkCheckState.getJSONObject("mucJwtDto");
        this.targetRegion = mucJwtDto.getString("targetRegion");
        this.domain = mucJwtDto.getString("domain");
    }

    public String getChatRoomName() {
        return chatRoomName;
    }

    public String getDomain() {
        return domain;
    }

    public String getTargetRegion() {
        return targetRegion;
    }

    @Override
    public String toString() {
        return "MatchContextPayload{" +
                "chatRoomName='" + chatRoomName + '\'' +
                ", domain='" + domain + '\'' +
                ", targetRegion='" + targetRegion + '\'' +
                '}';
    }
}
