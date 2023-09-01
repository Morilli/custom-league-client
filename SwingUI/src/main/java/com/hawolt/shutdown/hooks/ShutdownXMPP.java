package com.hawolt.shutdown.hooks;

import com.hawolt.client.LeagueClient;
import com.hawolt.shutdown.ShutdownTask;

/**
 * Created: 16/08/2023 18:06
 * Author: Twitter @hawolt
 **/

public class ShutdownXMPP extends ShutdownTask {
    public ShutdownXMPP(LeagueClient client) {
        super(client);
    }

    @Override
    protected void execute() throws Exception {
        client.getXMPPClient().terminate();
    }
}
