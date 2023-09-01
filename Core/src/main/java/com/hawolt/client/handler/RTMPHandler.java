package com.hawolt.client.handler;

import com.hawolt.client.LeagueClient;
import com.hawolt.generic.data.Platform;
import com.hawolt.generic.token.impl.StringTokenSupplier;
import com.hawolt.logger.Logger;
import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.virtual.leagueclient.authentication.impl.Sipt;
import com.hawolt.virtual.leagueclient.client.Authentication;
import com.hawolt.virtual.leagueclient.client.VirtualLeagueClient;
import com.hawolt.virtual.leagueclient.instance.IVirtualLeagueClientInstance;
import com.hawolt.virtual.riotclient.instance.IVirtualRiotClientInstance;
import com.hawolt.yaml.ConfigValue;
import com.hawolt.yaml.IYamlSupplier;
import com.hawolt.yaml.YamlWrapper;

import java.io.IOException;

/**
 * Created: 11/08/2023 18:42
 * Author: Twitter @hawolt
 **/

public class RTMPHandler {
    private final LeagueRtmpClient virtualLeagueRTMPClient;
    private final LeagueClient client;

    public static RTMPHandler build(LeagueClient client) throws IOException {
        VirtualLeagueClient virtualLeagueClient = client.getVirtualLeagueClient();
        IVirtualLeagueClientInstance virtualLeagueClientInstance = virtualLeagueClient.getVirtualLeagueClientInstance();
        IYamlSupplier yamlSupplier = client.getVirtualLeagueClientInstance().getYamlSupplier();
        YamlWrapper wrapper = yamlSupplier.getYamlResources(client.getPlayerPlatform());
        Platform platform = virtualLeagueClientInstance.getPlatform();
        Sipt sipt = new Sipt(
                client.getVirtualRiotClientInstance().getCookieSupplier(),
                client.getVirtualLeagueClientInstance().getPublicClientConfig()
        );
        IVirtualRiotClientInstance virtualRiotClientInstance = virtualLeagueClient.getVirtualRiotClientInstance();
        sipt.authenticate(
                virtualRiotClientInstance.getGateway(),
                virtualLeagueClientInstance.getLeagueClientUserAgent("rcp-lol-be-login"),
                virtualLeagueClient.get(Authentication.SESSION)
        );
        virtualLeagueClient.setAuthentication(Authentication.SIPT, sipt);
        String[] lcds = wrapper.get(ConfigValue.LCDS).split(":");
        return new RTMPHandler(client, platform.name(), lcds[0], Integer.parseInt(lcds[1]));
    }

    public RTMPHandler(LeagueClient client, String platform, String host, int port) {
        this.virtualLeagueRTMPClient = new LeagueRtmpClient(platform, host, port);
        this.client = client;
    }

    public RTMPHandler connect() {
        VirtualLeagueClient virtualLeagueClient = client.getVirtualLeagueClient();
        StringTokenSupplier rtmpSupplier = StringTokenSupplier.merge(
                "rtmp",
                virtualLeagueClient.get(Authentication.USERINFO),
                virtualLeagueClient.getVirtualLeagueClientInstance().getLeagueClientSupplier(),
                virtualLeagueClient.get(Authentication.SESSION),
                virtualLeagueClient.get(Authentication.SIPT)
        );

        this.virtualLeagueRTMPClient.connect(client.getVirtualLeagueClient().getVirtualRiotClient().getUsername(), rtmpSupplier);
        return this;
    }

    public LeagueRtmpClient getVirtualLeagueRTMPClient() {
        return virtualLeagueRTMPClient;
    }

    public void onError(Exception e) {
        Logger.error(e);
    }
}
