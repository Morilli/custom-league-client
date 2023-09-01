package com.hawolt.client.settings;

import org.json.JSONObject;

public abstract class SettingsObject extends JSONObject {
    protected SettingsObject() {
        super();
    };
    abstract protected void copyData(JSONObject json);
}
