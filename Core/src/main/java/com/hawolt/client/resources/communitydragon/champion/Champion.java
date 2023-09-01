package com.hawolt.client.resources.communitydragon.champion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created: 19/04/2023 16:37
 * Author: Twitter @hawolt
 **/

public class Champion {
    public static Champion DUMMY = new Champion(
            new JSONObject()
                    .put("id", -1)
                    .put("name", "None")
                    .put("alias", "None")
                    .put("roles", new JSONArray())
    );

    private final List<String> roles = new ArrayList<>();
    private final String name, alias;
    private final int id;

    public Champion(JSONObject o) {
        this.name = o.getString("name");
        this.alias = o.getString("alias");
        this.id = o.getInt("id");
        JSONArray roles = o.getJSONArray("roles");
        for (int i = 0; i < roles.length(); i++) {
            this.roles.add(roles.getString(i));
        }
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public int getId() {
        return id;
    }
}
