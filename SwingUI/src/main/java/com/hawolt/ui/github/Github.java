package com.hawolt.ui.github;

import com.hawolt.http.NativeHttpClient;
import com.hawolt.http.layer.IResponse;
import com.hawolt.logger.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;

public class Github {

    private static final String BASE_URL = "https://api.github.com/repos/hawolt/custom-league-client";

    private static JSONObject latestRelease;

    private Github() {
    }

    public static JSONObject getLatestRelease() {
        String url = BASE_URL + "/releases/latest";

        if (latestRelease == null) {
            latestRelease = doRequest(url);
        }

        return latestRelease;
    }

    private static JSONObject doRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        try {
            IResponse response = NativeHttpClient.execute(request);
            return new JSONObject(response.asString());
        } catch (IOException | InterruptedException e) {
            Logger.error("request failed to " + url);
            return new JSONObject();
        }
    }

    public static String getChangelog() {
        return getLatestRelease().get("body").toString();
    }

    public static String getVersion() {
        return getLatestRelease().get("name").toString();
    }
}
