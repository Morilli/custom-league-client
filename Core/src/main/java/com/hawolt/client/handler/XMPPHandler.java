package com.hawolt.client.handler;

import com.hawolt.client.LeagueClient;
import com.hawolt.logger.Logger;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.EventListener;
import com.hawolt.xmpp.event.EventType;
import com.hawolt.xmpp.event.handler.socket.ISocketListener;
import com.hawolt.xmpp.event.objects.other.PlainData;
import com.hawolt.xmpp.misc.impl.RiotDataCallback;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Created: 11/08/2023 18:42
 * Author: Twitter @hawolt
 **/

public class XMPPHandler implements ISocketListener, EventListener<PlainData> {
    private final VirtualRiotXMPPClient virtualRiotXMPPClient;
    private final RiotDataCallback riotDataCallback;
    private long timestamp;

    public XMPPHandler(RiotDataCallback riotDataCallback) {
        this.virtualRiotXMPPClient = new VirtualRiotXMPPClient(riotDataCallback);
        this.riotDataCallback = riotDataCallback;
    }

    public static XMPPHandler build(LeagueClient client) throws URISyntaxException, MalformedURLException {
        RiotDataCallback riotDataCallback = new RiotDataCallback(client.getVirtualLeagueClient());
        return new XMPPHandler(riotDataCallback);
    }

    public XMPPHandler connect() {
        this.virtualRiotXMPPClient.addHandler(EventType.ON_READY, this);
        this.virtualRiotXMPPClient.addSocketListener(this);
        this.virtualRiotXMPPClient.connect();
        return this;
    }

    public RiotDataCallback getRiotDataCallback() {
        return riotDataCallback;
    }

    public VirtualRiotXMPPClient getVirtualRiotXMPPClient() {
        return virtualRiotXMPPClient;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getConnectionIdentifier() {
        return null;
    }

    @Override
    public void onEvent(PlainData plainData) {
        this.timestamp = plainData.getTimestamp();
    }

    @Override
    public void onSessionRefreshFail() {
        Logger.info("XMPP session failed to refresh");
    }

    @Override
    public void onConnectionIssue() {
        Logger.info("XMPP encountered a connection issue");
    }

    @Override
    public void onSessionExpired() {
        Logger.info("XMPP session expired");
    }

    @Override
    public void onTermination() {
        Logger.info("XMPP terminated");
    }
}
