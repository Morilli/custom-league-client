package com.hawolt.client.resources.ledge.leagues;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.http.OkHttp3Client;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created: 19/01/2023 16:38
 * Author: Twitter @hawolt
 **/

public class LeagueLedge extends AbstractLedgeEndpoint {
    public LeagueLedge(LeagueClient client) {
        super(client);
    }

    public String getRankedOverviewToken() throws IOException {
        String uri = String.format("%s/%s/v%s/signedRankedStats",
                base,
                name(),
                version()
        );
        Request request = jsonRequest(uri).get().build();
        Call call = OkHttp3Client.perform(request, gateway);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                String plain = body.string();
                JSONObject object = new JSONObject(plain);
                return object.getString("jwt");
            }
        }
    }

    public JSONObject getRankedStats(String puuid) throws IOException {
        String uri = String.format("%s/%s/v%s/rankedStats/puuid/%s",
                base,
                name(),
                version(),
                puuid
        );
        Request request = jsonRequest(uri).get().build();
        Call call = OkHttp3Client.perform(request, gateway);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                String plain = body.string();
                return new JSONObject(plain);
            }
        }
    }

    @Override
    public int version() {
        return 2;
    }

    @Override
    public String name() {
        return "leagues-ledge";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-ranked";
    }
}
