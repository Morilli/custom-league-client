package com.hawolt.client.resources.communitydragon.champion;

import com.hawolt.http.layer.IResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created: 29/08/2023 21:29
 * Author: Twitter @hawolt
 **/

public class ChampionIndex {

    private final Map<Integer, Champion> map = new HashMap<>();

    public ChampionIndex() {

    }

    public ChampionIndex(IResponse response) {
        JSONArray array = new JSONArray(response.asString());
        for (int i = 0; i < array.length(); i++) {
            JSONObject reference = array.getJSONObject(i);
            Champion champion = new Champion(reference);
            map.put(champion.getId(), champion);
        }
    }

    public Champion getChampion(int id) {
        return map.getOrDefault(id, Champion.DUMMY);
    }
}
