package com.hawolt.client.resources.ledge.teambuilder;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheType;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.client.resources.ledge.teambuilder.objects.MatchContext;
import com.hawolt.generic.Constant;
import com.hawolt.http.OkHttp3Client;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created: 19/01/2023 16:04
 * Author: Twitter @hawolt
 **/

public class TeamBuilderLedge extends AbstractLedgeEndpoint {

    public TeamBuilderLedge(LeagueClient client) {
        super(client);
    }

    public MatchContext indicateAfkReadiness() throws IOException {
        String uri = String.format("%s/%s/v%s/indicateAfkReadiness/accountId/%s/summonerId/%s",
                base,
                name(),
                version(),
                client.getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeague().getCUID(),
                client.getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeagueAccount().getSummonerId()
        );
        JSONObject object = new JSONObject();
        object.put("additionalInventoryJwt", "");
        object.put("afkReady", true);
        object.put("initialSpellIds", new JSONArray().put(14).put(4));
        object.put("lastSelectedSkinIdByChampionId", new JSONObject());
        object.put("simplifiedInventoryJwt", client.getLedge().getInventoryService().getInventoryToken());
        Request request = jsonRequest(uri)
                .post(RequestBody.create(object.toString(), Constant.APPLICATION_JSON))
                .build();
        Call call = OkHttp3Client.perform(request, gateway);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                MatchContext context = new MatchContext(new JSONObject(body.string()));
                client.cache(CacheType.MATCH_CONTEXT, context);
                return context;
            }
        }
    }

    @Override
    public int version() {
        return 2;
    }

    @Override
    public String name() {
        return "team-builder-ledge";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-lobby-team-builder";
    }

}
