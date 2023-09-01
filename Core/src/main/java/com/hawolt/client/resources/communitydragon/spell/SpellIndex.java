package com.hawolt.client.resources.communitydragon.spell;

import com.hawolt.http.layer.IResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created: 29/08/2023 21:29
 * Author: Twitter @hawolt
 **/

public class SpellIndex {

    private final Map<Integer, Spell> map = new HashMap<>();

    public SpellIndex() {

    }

    public SpellIndex(IResponse response) {
        JSONArray array = new JSONArray(response.asString());
        for (int i = 0; i < array.length(); i++) {
            JSONObject reference = array.getJSONObject(i);
            Spell spell = new Spell(reference);
            map.put(spell.getId(), spell);
        }
    }

    public Spell[] getAvailableSpells() {
        return map.values().toArray(new Spell[0]);
    }

    public Spell getSpell(int id) {
        return map.getOrDefault(id, Spell.DUMMY);
    }
}
