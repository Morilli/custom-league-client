package com.hawolt.client.resources.ledge.preferences;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.misc.Base64RawInflate;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.generic.Constant;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Map;

public class PlayerPreferencesLedge extends AbstractLedgeEndpoint {
    public PlayerPreferencesLedge(LeagueClient client) {
        super(client);
    }

    public JSONObject getPlayerPreferences() throws IOException {
        String uri = String.format("https://playerpreferences.riotgames.com/%s/v%s/getPreference/%s/%s/LCUPreferences/",
                name(),
                version(),
                userInformation.getPvpnetAccountId(),
                client.getVirtualLeagueClientInstance().getPlatform()
        );
        Request request = jsonRequest(uri)
                .get()
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        JSONObject jsonObject = new JSONObject(response.asString());
        String data = jsonObject.getString("data");
        byte[] base64Decoded = Base64RawInflate.decode(data);
        String inflated = Base64RawInflate.inflate(base64Decoded);
        return convertYamlToJson(inflated);
    }

    public int setPlayerPreferences(String data) throws IOException {
        String uri = String.format("https://playerpreferences.riotgames.com/%s/v%s/savePreference/%s/%s",
                name(),
                version(),
                userInformation.getPvpnetAccountId(),
                client.getVirtualLeagueClientInstance().getPlatform()
        );
        String s = Base64RawInflate.encode(Base64RawInflate.deflate(convertJsonToYaml(new JSONObject(data)).getBytes()));
        JSONObject sendData = new JSONObject();
        sendData.put("data", s);
        sendData.put("type", "LCUPreferences");
        sendData.put("version", "1.0");
        Request request = jsonRequest(uri)
                .put(RequestBody.create(sendData.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return response.code();
    }

    private JSONObject convertYamlToJson(String yamlString) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(yamlString);
        return new JSONObject(map);
    }

    private String convertJsonToYaml(JSONObject jsonObject) {
        Yaml yaml = new Yaml();
        String JSONString = jsonObject.toString(4);
        Map<String, Object> map = yaml.load(JSONString);
        return yaml.dump(map);
    }

    @Override
    public int version() {
        return 3;
    }

    @Override
    public String name() {
        return "playerPref";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-player-preferences";
    }

    @Override
    public String auth() {
        return String.join(" ", "Bearer", client.getVirtualLeagueClientInstance().getLeagueClientSupplier().getSimple("access_token"));
    }
}
