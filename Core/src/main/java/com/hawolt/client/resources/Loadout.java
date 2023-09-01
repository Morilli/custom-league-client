package com.hawolt.client.resources;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.client.resources.ledge.inventory.InventoryServiceLedge;
import com.hawolt.generic.Constant;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.logger.Logger;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;


public class Loadout extends AbstractLedgeEndpoint {
    private final InventoryServiceLedge inventoryServiceLedge;

    public Loadout(LeagueClient leagueClient) {
        super(leagueClient);
        this.inventoryServiceLedge = leagueClient.getLedge().getInventoryService();
    }


    private JSONArray getBundledJWTs() throws IOException {
        JSONArray array = new JSONArray();
        array.put(inventoryServiceLedge.getInventoryJwt("COMPANION"));
        array.put(inventoryServiceLedge.getInventoryJwt("REGALIA_BANNER"));
        array.put(inventoryServiceLedge.getInventoryJwt("WARD_SKIN"));
        array.put(inventoryServiceLedge.getInventoryJwt("EMOTE"));
        array.put(inventoryServiceLedge.getInventoryJwt("MODE_PROGRESSION_REWARD"));
        array.put(inventoryServiceLedge.getInventoryJwt("REGALIA_CREST"));
        array.put(inventoryServiceLedge.getInventoryJwt("TFT_DAMAGE_SKIN"));
        array.put(inventoryServiceLedge.getInventoryJwt("TOURNAMENT_TROPHY"));
        array.put(inventoryServiceLedge.getInventoryJwt("TFT_MAP_SKIN"));
        array.put(inventoryServiceLedge.getInventoryJwt("TOURNAMENT_FRAME"));
        array.put(inventoryServiceLedge.getInventoryJwt("TFT_PLAYBOOK"));
        array.put(inventoryServiceLedge.getInventoryJwt("TOURNAMENT_FLAG"));
        return array;
    }

    public JSONObject getLoadout() throws IOException {
        JSONArray array = getBundledJWTs();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v" + version())
                .addPathSegment("loadouts")
                .addPathSegment("scope")
                .addPathSegment("ACCOUNT")
                .addPathSegment("get")
                .addQueryParameter("playerId", userInformation.getSub())
                .build();
        JSONObject lolinventory = new JSONObject();
        JSONObject serviceToJwtsMap = new JSONObject();
        serviceToJwtsMap.put("serviceToJwtsMap", lolinventory);
        lolinventory.put("lolinventory", array);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", auth())
                .addHeader("User-Agent", agent())
                .addHeader("Accept", "application/json")
                .post(RequestBody.create(serviceToJwtsMap.toString(), Constant.APPLICATION_JSON))
                .build();
        Call call = OkHttp3Client.perform(request, gateway);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                String plain = body.string();
                JSONArray arr = new JSONArray(plain);
                JSONObject object;
                object = arr.getJSONObject(0);
                return object;
            }
        }
    }

    public int getLegend() throws IOException {
        JSONObject object = getLoadout();
        JSONObject loadout = object.getJSONObject("loadout");
        if (!loadout.isNull("TFT_PLAYBOOK_SLOT")) {
            JSONObject tftPlaybookSlot = loadout.getJSONObject("TFT_PLAYBOOK_SLOT");
            return (int) tftPlaybookSlot.get("itemId");
        }
        return 1;
    }

    public void setLegend(int itemId, String contentId) throws IOException {
        JSONObject loadoutParent = new JSONObject();
        JSONObject object = getLoadout();
        loadoutParent.put("loadout", object);
        JSONObject serviceToJwtsMap = new JSONObject();
        loadoutParent.put("serviceToJwtsMap", serviceToJwtsMap);
        JSONArray array = getBundledJWTs();
        serviceToJwtsMap.put("lolinventory", array);
        JSONObject loadout = object.getJSONObject("loadout");
        if (!loadout.isNull("TFT_PLAYBOOK_SLOT")) {
            JSONObject tftPlaybookSlot = loadout.getJSONObject("TFT_PLAYBOOK_SLOT");
            tftPlaybookSlot.remove("contentId");
            tftPlaybookSlot.remove("itemId");
            tftPlaybookSlot.put("itemId", itemId);
            tftPlaybookSlot.put("contentId", contentId);
        } else {
            JSONObject tftPlaybookSlot = new JSONObject();
            tftPlaybookSlot.put("inventoryType", "TFT_PLAYBOOK");
            tftPlaybookSlot.put("itemId", itemId);
            tftPlaybookSlot.put("contentId", contentId);
            loadout.put("TFT_PLAYBOOK_SLOT", tftPlaybookSlot);
        }
        loadout.remove("scope");
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v" + version())
                .addPathSegment("loadouts")
                .addPathSegment(getLoadoutID())
                .addQueryParameter("playerId", userInformation.getSub())
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", auth())
                .addHeader("User-Agent", agent())
                .addHeader("Accept", "application/json")
                .put(RequestBody.create(loadoutParent.toString(), Constant.APPLICATION_JSON))
                .build();
        Call call = OkHttp3Client.perform(request, gateway);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                String plain = body.string();
                JSONObject content = new JSONObject(plain);
                Logger.debug(content);
            }
        }
    }

    public String getLoadoutID() throws IOException {
        JSONArray array = getBundledJWTs();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v" + version())
                .addPathSegment("loadouts")
                .addPathSegment("scope")
                .addPathSegment("ACCOUNT")
                .addPathSegment("get")
                .addQueryParameter("playerId", userInformation.getSub())
                .build();
        JSONObject lolinventory = new JSONObject();
        JSONObject serviceToJwtsMap = new JSONObject();
        serviceToJwtsMap.put("serviceToJwtsMap", lolinventory);
        lolinventory.put("lolinventory", array);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", auth())
                .addHeader("User-Agent", agent())
                .addHeader("Accept", "application/json")
                .post(RequestBody.create(serviceToJwtsMap.toString(), Constant.APPLICATION_JSON))
                .build();
        Call call = OkHttp3Client.perform(request, gateway);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                String plain = body.string();
                JSONArray array1 = new JSONArray(plain);
                JSONObject object;
                object = array1.getJSONObject(0);
                return object.get("id").toString();
            }
        }
    }

    public String name() {
        return "loadouts";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-loadouts";
    }

    public int version() {
        return 4;
    }

    @Override
    public String auth() {
        return String.join(" ", "Bearer", client.getVirtualLeagueClientInstance().getLeagueClientSupplier().getSimple("access_token"));
    }
}
