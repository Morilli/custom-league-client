package com.hawolt.settings;

import com.hawolt.virtual.misc.DynamicObject;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created: 28/08/2023 19:49
 * Author: Twitter @hawolt
 **/

public class UserSettings extends DynamicObject {
    public UserSettings(JSONObject o) {
        super(o);
    }

    public JSONArray getCookies() {
        return getByKeyNonNullOrThrow("cookies", () -> new RuntimeException("NO_COOKIES_PRESENT"));
    }
}
