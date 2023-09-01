package com.hawolt.client.resources.communitydragon.skin;

import com.hawolt.http.layer.IResponse;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created: 29/08/2023 21:29
 * Author: Twitter @hawolt
 **/

public class SkinIndex {

    private final Map<Integer, Integer> reverse = new HashMap<>();
    private final Map<Integer, Skin> map = new HashMap<>();

    public SkinIndex() {

    }

    public SkinIndex(IResponse response) {
        JSONObject object = new JSONObject(response.asString());
        for (String key : object.keySet()) {
            JSONObject reference = object.getJSONObject(key);
            Skin skin = new Skin(reference);
            for (int chromaId : skin.getChromas()) {
                reverse.put(chromaId, skin.getId());
            }
            map.put(skin.getId(), skin);
        }
    }

    public Skin getSkin(int id) {
        int skinId = !reverse.containsKey(id) ? id : reverse.getOrDefault(id, -1);
        return map.getOrDefault(skinId, Skin.DUMMY);
    }
}
