package com.hawolt.client.resources.ledge.teambuilder.objects;

import org.json.JSONObject;

/**
 * Created: 14/08/2023 19:36
 * Author: Twitter @hawolt
 **/

public class MatchContext {
    private final MatchContextPayload payload;
    private final String status;

    public MatchContext(JSONObject o) {
        this.status = o.getString("status");
        this.payload = new MatchContextPayload(o.getJSONObject("payload"));
    }

    public MatchContextPayload getPayload() {
        return payload;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "MatchContext{" +
                "payload=" + payload +
                ", status='" + status + '\'' +
                '}';
    }
}
