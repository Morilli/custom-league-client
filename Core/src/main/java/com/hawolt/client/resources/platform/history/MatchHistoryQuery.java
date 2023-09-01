package com.hawolt.client.resources.platform.history;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.platform.AbstractPlatformEndpoint;
import com.hawolt.client.resources.platform.history.data.MatchGameMode;
import com.hawolt.client.resources.platform.history.data.MatchResponseType;
import com.hawolt.client.resources.platform.history.object.MatchOutcomeDetails;
import com.hawolt.client.resources.platform.history.object.MatchOutcomeSummary;
import com.hawolt.generic.data.Platform;
import com.hawolt.generic.token.impl.StringTokenSupplier;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.version.IVersionSupplier;
import com.hawolt.virtual.clientconfig.impl.redge.RedgeType;
import com.hawolt.virtual.leagueclient.client.Authentication;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created: 14/01/2023 01:24
 * Author: Twitter @hawolt
 **/


public class MatchHistoryQuery extends AbstractPlatformEndpoint {

    private final StringTokenSupplier tokenSupplier;
    private final IVersionSupplier versionSupplier;

    private final Platform platform;

    public MatchHistoryQuery(LeagueClient client) {
        super(client);
        this.tokenSupplier = virtualLeagueClient.get(Authentication.SESSION);
        this.platform = client.getVirtualLeagueClientInstance().getPlatform();
        this.versionSupplier = client.getVirtualLeagueClientInstance().getLocalLeagueFileVersion();
        this.base = client.getVirtualLeagueClientInstance()
                .getPublicClientConfig()
                .getRedgeConfig()
                .getRedgeValue(RedgeType.MATCH_HISTORY_QUERY);
    }

    public MatchOutcomeDetails getGameDetails(MatchGameMode matchGameMode, long gameId) throws IOException {
        try (Response response = getGame(matchGameMode, gameId, MatchResponseType.DETAILS)) {
            return new MatchOutcomeDetails(new JSONObject(response.body().string()));
        }
    }

    public MatchOutcomeSummary getGameSummary(MatchGameMode matchGameMode, long gameId) throws IOException {
        try (Response response = getGame(matchGameMode, gameId, MatchResponseType.SUMMARY)) {
            JSONObject object = new JSONObject(response.body().string());
            return new MatchOutcomeSummary(object);
        }
    }

    public Response getGame(MatchGameMode matchGameMode, long gameId, MatchResponseType matchResponseType) throws IOException {
        String auth = String.format("Bearer %s", tokenSupplier.get("session.session_token", true));
        String agent = String.format("LeagueOfLegendsClient/%s (rcp-be-lol-match-history)",
                versionSupplier.getVersionValue(platform, "LeagueClientUxRender.exe")
        );
        String uri = String.format("%s/%s/v%s/products/%s/%s_%s/%s",
                base,
                name(),
                version(),
                matchGameMode.toString(),
                platform.name().toUpperCase(),
                gameId,
                matchResponseType.name()
        );
        Request request = new Request.Builder()
                .url(uri)
                .addHeader("Authorization", auth)
                .addHeader("User-Agent", agent)
                .addHeader("Accept", "application/json")
                .get()
                .build();
        Call call = OkHttp3Client.perform(request, gateway);
        return call.execute();
    }

    public List<MatchOutcomeSummary> getHistorySummary(MatchGameMode matchGameMode, String puuid) throws IOException {
        try (Response response = getHistory(matchGameMode, puuid, true)) {
            List<MatchOutcomeSummary> list = new ArrayList<>();
            JSONObject object = new JSONObject(response.body().string());
            JSONArray games = object.getJSONArray("games");
            for (int i = 0; i < games.length(); i++) {
                try {
                    list.add(new MatchOutcomeSummary(games.getJSONObject(i)));
                } catch (RuntimeException e) {
                    //TODO ignored
                }
            }
            return list;
        }
    }

    public String[] getHistory(MatchGameMode matchGameMode, String puuid) throws IOException {
        try (Response response = getHistory(matchGameMode, puuid, false)) {
            JSONArray array = new JSONArray(response.body().string());
            String[] games = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                games[i] = array.getString(i);
            }
            return games;
        }
    }

    public Response getHistory(MatchGameMode matchGameMode, String puuid, boolean summary) throws IOException {
        String auth = String.format("Bearer %s", tokenSupplier.get("session.session_token", true));
        String agent = String.format("LeagueOfLegendsClient/%s (%s)",
                versionSupplier.getVersionValue(platform, "LeagueClientUxRender.exe"),
                rcp()
        );
        String uri = String.format("%s/%s/v%s/products/%s/player/%s%s",
                base,
                name(),
                version(),
                matchGameMode.toString(),
                puuid,
                summary ? "/SUMMARY" : ""
        );
        Request request = new Request.Builder()
                .url(uri)
                .addHeader("Authorization", auth)
                .addHeader("User-Agent", agent)
                .addHeader("Accept", "application/json")
                .get()
                .build();
        Call call = OkHttp3Client.perform(request, gateway);
        return call.execute();
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public String name() {
        return "match-history-query";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-match-history";
    }
}
