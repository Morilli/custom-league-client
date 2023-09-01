package com.hawolt.client.resources.ledge.summoner;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.client.resources.ledge.summoner.objects.SummonerValidation;
import com.hawolt.generic.Constant;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Created: 19/01/2023 16:38
 * Author: Twitter @hawolt
 **/

public class SummonerLedge extends AbstractLedgeEndpoint {
    public SummonerLedge(LeagueClient client) {
        super(client);
    }

    public Summoner resolveSummonerByPUUD(String name) throws IOException {
        return resolveSummoner(
                String.format("%s/%s/v%s/regions/%s/summoners/puuid/%s",
                        base,
                        name(),
                        version(),
                        platform.name().toLowerCase(),
                        URLEncoder.encode(name, StandardCharsets.UTF_8.name())
                )
        );
    }

    public Summoner resolveSummonerByName(String name) throws IOException {
        return resolveSummoner(
                String.format("%s/%s/v%s/regions/%s/summoners/name/%s",
                        base,
                        name(),
                        version(),
                        platform.name().toLowerCase(),
                        URLEncoder.encode(name, StandardCharsets.UTF_8.name())
                )
        );
    }

    public Summoner resolveSummoner(String uri) throws IOException {
        Request request = jsonRequest(uri)
                .get()
                .build();
        Call call = OkHttp3Client.perform(request, gateway);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                String plain = body.string();
                JSONObject o = new JSONObject(plain);
                return new Summoner(o);
            }
        }
    }

    public String getSummonerToken() throws IOException {
        String uri = String.format("%s/%s/v%s/regions/%s/summoners/puuid/%s/jwt",
                base,
                name(),
                version(),
                platform.name().toLowerCase(),
                userInformation.getSub()
        );
        Request request = jsonRequest(uri)
                .get()
                .build();
        Call call = OkHttp3Client.perform(request, gateway);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                String plain = body.string();
                return plain.substring(1, plain.length() - 1);
            }
        }
    }

    public SummonerValidation validateSummonerName(String name) throws IOException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v" + version())
                .addPathSegment("regions")
                .addPathSegment(platform.name().toLowerCase())
                .addPathSegment("validatename")
                .addQueryParameter("summonerName", name)
                .build();
        Request request = jsonRequest(url)
                .get()
                .build();
        Call call = OkHttp3Client.perform(request, gateway);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                String plain = body.string();
                return new SummonerValidation(new JSONArray(plain));
            }
        }
    }

    public Summoner claimSummonerName(String name) throws IOException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v" + version())
                .addPathSegment("regions")
                .addPathSegment(platform.name().toLowerCase())
                .addPathSegment("summoners")
                .addPathSegment("puuid")
                .addPathSegment(client.getVirtualLeagueClientInstance().getUserInformation().getSub())
                .build();
        JSONObject object = new JSONObject();
        object.put("summonerName", name);
        Request request = jsonRequest(url)
                .post(RequestBody.create(object.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new Summoner(new JSONObject(response.asString()));
    }


    @Override
    public int version() {
        return 1;
    }

    @Override
    public String name() {
        return "summoner-ledge";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-summoner";
    }
}
