package com.hawolt.client.resources.ledge.gsm.objects;

import com.hawolt.client.Launcher;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.gsm.GameServiceMessageLedge;
import com.hawolt.generic.data.Platform;
import com.hawolt.logger.Logger;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created: 22/08/2023 16:53
 * Author: Twitter @hawolt
 **/

public class ActiveGameInformation implements Runnable {
    private final GameServiceMessageLedge ledge;
    private final Platform platform;

    public ActiveGameInformation(LeagueClient client) {
        this.ledge = client.getLedge().getGameServiceMessage();
        this.platform = client.getPlayerPlatform();
    }

    @Override
    public void run() {
        try {
            JSONObject info = ledge.getCurrentGameInformation();
            if (!info.has("game")) return;
            JSONObject game = info.getJSONObject("game");
            if (!game.has("gameState") || game.isNull("gameState")) return;
            if (!"IN_PROGRESS".equals(game.getString("gameState"))) return;
            JSONObject credentials = info.getJSONObject("playerCredentials");
            Launcher.launch(platform, credentials);
        } catch (IOException e) {
            Logger.error(e);
        }
    }
}
