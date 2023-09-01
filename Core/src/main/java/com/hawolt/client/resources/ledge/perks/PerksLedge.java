package com.hawolt.client.resources.ledge.perks;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.client.resources.ledge.parties.objects.PartiesRegistration;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.generic.Constant;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.virtual.leagueclient.userinfo.child.UserInformationLeagueAccount;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created: 19/01/2023 16:38
 * Author: Twitter @hawolt
 **/

public class PerksLedge extends AbstractLedgeEndpoint {
    public PerksLedge(LeagueClient client) {
        super(client);
    }

    private static String escape(String raw) {
        String escaped = raw;
        escaped = escaped.replace("\\", "\\\\");
        escaped = escaped.replace("\"", "\\\"");
        escaped = escaped.replace("\b", "\\b");
        escaped = escaped.replace("\f", "\\f");
        escaped = escaped.replace("\n", "\\n");
        escaped = escaped.replace("\r", "\\r");
        escaped = escaped.replace("\t", "\\t");
        // TODO: escape other non-printing characters using uXXXX notation
        return "\"" + escaped + "\"";
    }

    public String setRunesForCurrentRegistration(JSONObject runes) throws IOException {
        PartiesRegistration registration = client.getLedge().getParties().getCurrentRegistration();
        int queueId = registration.getCurrentParty().getPartyGameMode().getQueueId();

        UserInformationLeagueAccount account = userInformation.getUserInformationLeagueAccount();
        Summoner self = client.getLedge().getSummoner().resolveSummonerByName(account.getSummonerName());

        String uri = String.format("%s/%s/v%s/queues/%s/customizations/perks/accountId/%s/summonerId/%s",
                base,
                name(),
                version(),
                queueId,
                self.getAccountId(),
                self.getSummonerId()
        );

        Request request = jsonRequest(uri)
                .post(RequestBody.create(escape(runes.toString()), Constant.APPLICATION_JSON))
                .build();
        Call call = OkHttp3Client.perform(request, gateway);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                return body.string();
            }
        }
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public String name() {
        return "perks-edge";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-perks";
    }
}
