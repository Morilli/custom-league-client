package com.hawolt.client.settings.login;

import com.hawolt.client.settings.SettingsObject;
import org.json.JSONObject;

public class LoginSettings extends SettingsObject {
    @Override
    protected void copyData(JSONObject json) {
        setUsername(json.getString("username"));
        setPassword(json.getString("password"));
        setRememberMe(json.getBoolean("rememberMe"));
    }

    public String getUsername() {
        return getString("username");
    }

    public LoginSettings setUsername(String username) {
        put("username", username);
        return this;
    }

    public String getPassword() {
        return getString("password");
    }

    public LoginSettings setPassword(String password) {
        put("password", password);
        return this;
    }

    public boolean getRememberMe() {
        return getBoolean("rememberMe");
    }

    public LoginSettings setRememberMe(boolean rememberMe) {
        put("rememberMe", rememberMe);
        return this;
    }
}
