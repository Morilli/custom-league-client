package com.hawolt.client.resources.ledge.loot;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.client.resources.ledge.loot.objects.Loot;
import com.hawolt.client.resources.ledge.loot.objects.LootAction;
import com.hawolt.client.resources.ledge.loot.objects.PlayerLoot;
import com.hawolt.generic.Constant;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

/**
 * Created: 27/07/2023 22:21
 * Author: Twitter @hawolt
 **/

public class LootLedge extends AbstractLedgeEndpoint {
    public LootLedge(LeagueClient client) {
        super(client);
    }

    //TODO hardcoded location fix
    public JSONObject interact(Loot loot, LootAction action) throws IOException {
        return interact(loot, action, 1);
    }

    public JSONObject interact(Loot loot, LootAction action, int repeat) throws IOException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v" + version())
                .addPathSegment("playerloot")
                .addPathSegment("location")
                .addPathSegment(String.format("lolriot.aws-euc1-prod.%s", platform.name().toLowerCase()))
                .addPathSegment("craftref")
                .addPathSegment("id")
                .addPathSegment(UUID.randomUUID().toString())
                .build();
        JSONObject object = new JSONObject();
        object.put("accountId", client.getVirtualLeagueClientInstance().getUserInformation().getOriginalAccountId());
        object.put("clientId", "LolClient-LEdge");
        object.put("playerId", client.getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeagueAccount().getSummonerId());
        object.put("puuid", client.getVirtualLeagueClientInstance().getUserInformation().getSub());
        if (loot.getLootName().contains("CHAMPION_SKIN")) {
            object.put("recipeName", String.join(
                    "_",
                    loot.getLootName().substring(
                            loot.getLootName().indexOf("_") + 1,
                            loot.getLootName().lastIndexOf("_")
                    ),
                    LootAction.DISENCHANT.name().toLowerCase()
            ));
        } else {
            object.put("recipeName", String.join(
                    "_",
                    loot.getLootName().substring(0, loot.getLootName().lastIndexOf("_")),
                    LootAction.DISENCHANT.name().toLowerCase())
            );
        }
        object.put("repeat", repeat);
        JSONArray array = new JSONArray();
        JSONObject instance = new JSONObject();
        instance.put("lootName", loot.getLootName());
        instance.put("refId", "");
        array.put(instance);
        if (action == LootAction.UPGRADE) {
            JSONObject upgrade = new JSONObject();
            String type = loot.getLootName().contains("SKIN") ? "SKIN" : "CHAMPION";
            upgrade.put("lootName", String.join("_", type, action.name().toLowerCase()));
            upgrade.put("refId", "");
            array.put(upgrade);
        }
        object.put("lootNameRefIds", array);
        Request request = jsonRequest(url)
                .post(RequestBody.create(object.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new JSONObject(response.asString());
    }

    public PlayerLoot get() throws IOException {
        String timestamp = String.valueOf(System.currentTimeMillis());
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v" + version())
                .addPathSegment("playerlootdefinitions")
                .addPathSegment("location")
                .addPathSegment(String.format("lolriot.aws-euc1-prod.%s", platform.name().toLowerCase()))
                .addPathSegment("playerId")
                .addPathSegment(String.valueOf(client.getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeagueAccount().getSummonerId()))
                .addQueryParameter("lastLootItemUpdate", timestamp)
                .addQueryParameter("lastRecipeUpdate", timestamp)
                .addQueryParameter("lastQueryUpdate", timestamp)
                .build();
        Request request = jsonRequest(url).get().build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new PlayerLoot(new JSONObject(response.asString()).getJSONArray("playerLoot"));
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public String name() {
        return "loot";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-loot";
    }
}
