package com.hawolt.async.loader;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.ExceptionalSupplier;
import com.hawolt.client.resources.ledge.preferences.PlayerPreferencesLedge;

import java.nio.charset.StandardCharsets;

/**
 * Created: 28/08/2023 20:28
 * Author: Twitter @hawolt
 **/

public class PreferenceLoader implements ExceptionalSupplier<byte[]> {
    private final LeagueClient client;

    public PreferenceLoader(LeagueClient client) {
        this.client = client;
    }

    @Override
    public byte[] get() throws Exception {
        PlayerPreferencesLedge playerPreferences = client.getLedge().getPlayerPreferences();
        return playerPreferences.getPlayerPreferences().toString().getBytes(StandardCharsets.UTF_8);
    }
}
