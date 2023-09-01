package com.hawolt.client.resources.platform;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.platform.history.MatchHistoryQuery;

/**
 * Created: 14/01/2023 04:57
 * Author: Twitter @hawolt
 **/

public class PlatformEndpoint {

    private final MatchHistoryQuery matchHistoryQuery;

    public PlatformEndpoint(LeagueClient client) {
        this.matchHistoryQuery = new MatchHistoryQuery(client);
    }

    public MatchHistoryQuery getMatchHistoryQuery() {
        return matchHistoryQuery;
    }
}
