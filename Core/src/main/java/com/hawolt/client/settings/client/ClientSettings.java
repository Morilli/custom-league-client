package com.hawolt.client.settings.client;

import com.hawolt.client.settings.SettingsObject;
import org.json.JSONObject;

public class ClientSettings extends SettingsObject {
    public enum Key {
        GAME_BASE_DIRECTORY("GameBaseDir");

        private String key;
        private Key(String key) {
            this.key = key;
        }

        public String get() {
            return key;
        }
    }

    public ClientSettings() {
        super();
        put(Key.GAME_BASE_DIRECTORY.get(), "C:\\Riot Games\\League of Legends");
    }

    @Override
    protected void copyData(JSONObject json) {
        for (Key key : Key.values()) {
            put(key.get(), json.getString(key.get()));
        }
    }

    public String getGameBaseDir() {
        return getString(Key.GAME_BASE_DIRECTORY.get());
    }
}