package com.hawolt.client.resources.communitydragon.skin;

import com.hawolt.client.resources.communitydragon.CommunityDragonSource;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import com.hawolt.logger.Logger;
import okhttp3.Request;

import java.io.IOException;

/**
 * Created: 29/08/2023 20:11
 * Author: Twitter @hawolt
 **/

public class SkinSource implements CommunityDragonSource<SkinIndex> {

    public static final SkinSource SKIN_SOURCE_INSTANCE = new SkinSource();

    private SkinIndex cache;

    @Override
    public String getSource() {
        return "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/skins.json";
    }

    @Override
    public SkinIndex get() {
        if (cache != null) return cache;
        Request request = new Request.Builder()
                .url(getSource())
                .header("User-Agent", "hawolt-custom-client-core")
                .get()
                .build();
        try {
            IResponse response = OkHttp3Client.execute(request);
            cache = new SkinIndex(response);
        } catch (IOException e) {
            Logger.warn("failed to load {}", getClass().getSimpleName());
        }
        return cache != null ? cache : new SkinIndex();
    }
}
