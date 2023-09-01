package com.hawolt.client.resources.communitydragon.skin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created: 30/08/2023 19:27
 * Author: Twitter @hawolt
 **/

public class Skin {
    public static Skin DUMMY = new Skin(
            new JSONObject()
                    .put("id", -1)
                    .put("name", "None")
                    .put("isBase", true)
    );
    private final Set<Integer> chromas = new HashSet<>();
    private final boolean base;
    private final String name;
    private final int id;

    public Skin(JSONObject o) {
        this.id = o.getInt("id");
        this.name = o.getString("name");
        this.base = o.getBoolean("isBase");
        if (!o.has("chromas")) return;
        JSONArray chromas = o.getJSONArray("chromas");
        for (int i = 0; i < chromas.length(); i++) {
            JSONObject chroma = chromas.getJSONObject(i);
            this.chromas.add(chroma.getInt("id"));
        }
    }

    public Set<Integer> getChromas() {
        return chromas;
    }

    public boolean isBase() {
        return base;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
