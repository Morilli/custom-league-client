package com.hawolt.client.resources.communitydragon.spell;

import org.json.JSONObject;

/**
 * Created: 30/08/2023 20:00
 * Author: Twitter @hawolt
 **/

public class Spell {
    public static Spell DUMMY = new Spell(
            new JSONObject()
                    .put("id", -1)
                    .put("iconPath", "/dummy.jpg")
                    .put("name", "dummy")
    );
    public final static String BASE = "https://raw.communitydragon.org/latest/game/data/spells/icons2d";
    private final String name, iconName;
    private String iconPath;
    private final int id;

    public Spell(JSONObject o) {
        this.id = o.getInt("id");
        this.iconPath = o.getString("iconPath");
        this.iconName = iconPath.substring(iconPath.lastIndexOf("/") + 1).toLowerCase();
        this.iconPath = String.join("/", BASE, iconName);
        this.name = o.getString("name");
    }

    public int getId() {
        return id;
    }

    public String getIconPath() {
        return iconPath;
    }

    public String getName() {
        return name;
    }

    public String getIconName() {
        return iconName;
    }

    @Override
    public String toString() {
        return name;
    }
}
