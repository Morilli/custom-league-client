package com.hawolt.client.handler;

import com.hawolt.client.LeagueClient;
import com.hawolt.logger.Logger;
import com.hawolt.rms.IRiotMessageServiceConnectionCallback;
import com.hawolt.rms.VirtualRiotMessageClient;
import com.hawolt.rms.data.GenericRiotMessageEvent;
import org.java_websocket.handshake.ServerHandshake;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Created: 11/08/2023 18:22
 * Author: Twitter @hawolt
 **/

public class RMSHandler implements IRiotMessageServiceConnectionCallback {
    private final VirtualRiotMessageClient virtualRiotMessageClient;
    private final LeagueClient client;

    public RMSHandler(LeagueClient client) {
        this.client = client;
        this.virtualRiotMessageClient = VirtualRiotMessageClient.create(client.getVirtualLeagueClient(), this);
    }

    public static RMSHandler build(LeagueClient client) throws URISyntaxException, MalformedURLException {
        return new RMSHandler(client);
    }

    public RMSHandler connect() {
        this.virtualRiotMessageClient.connect();
        return this;
    }

    public VirtualRiotMessageClient getVirtualRiotMessageClient() {
        return virtualRiotMessageClient;
    }

    @Override
    public void onRiotMessageEvent(GenericRiotMessageEvent genericRiotMessageEvent) {
        Logger.info(genericRiotMessageEvent);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        Logger.info("RMS connection closed: {}, {}, {}", i, s, b);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Logger.info("Connected to RMS: {}", serverHandshake.getHttpStatusMessage());
    }

    @Override
    public void onError(Exception e) {
        Logger.error(e);
    }
}
