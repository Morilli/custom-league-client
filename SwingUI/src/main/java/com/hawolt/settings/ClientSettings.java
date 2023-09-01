package com.hawolt.settings;

import com.hawolt.virtual.misc.DynamicObject;
import org.json.JSONObject;

/**
 * Created: 28/08/2023 19:48
 * Author: Twitter @hawolt
 **/

public class ClientSettings extends DynamicObject {
    public ClientSettings(JSONObject o) {
        super(o);
    }

    public boolean isRememberMe() {
        return getByKeyOrDefault("remember", false);
    }

    public String getRememberMeUsername() {
        return getByKeyNonNullOrThrow("username", () -> new RuntimeException("NO_USERNAME_PRESENT"));
    }
}
