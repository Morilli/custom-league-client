package com.hawolt.ui.champselect.util;

import org.json.JSONArray;

import java.util.function.Function;

/**
 * Created: 30/08/2023 17:19
 * Author: Twitter @hawolt
 **/

public class MemberFunction implements Function<JSONArray, ChampSelectMember[]> {
    public static MemberFunction INSTANCE = new MemberFunction();

    @Override
    public ChampSelectMember[] apply(JSONArray array) {
        ChampSelectMember[] members = new ChampSelectMember[array.length()];
        for (int i = 0; i < array.length(); i++) {
            members[i] = new ChampSelectMember(array.getJSONObject(i));
        }
        return members;
    }
}
