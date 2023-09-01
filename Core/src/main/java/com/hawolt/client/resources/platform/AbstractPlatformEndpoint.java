package com.hawolt.client.resources.platform;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.UndocumentedEndpoint;
import com.hawolt.generic.data.Platform;
import com.hawolt.generic.token.impl.StringTokenSupplier;
import com.hawolt.version.IVersionSupplier;
import com.hawolt.virtual.leagueclient.client.Authentication;
import com.hawolt.virtual.leagueclient.userinfo.UserInformation;

/**
 * Created: 20/01/2023 17:44
 * Author: Twitter @hawolt
 **/

public abstract class AbstractPlatformEndpoint extends UndocumentedEndpoint {
    protected final StringTokenSupplier tokenSupplier;
    protected final IVersionSupplier versionSupplier;
    protected final UserInformation userInformation;
    protected final Platform platform;

    public AbstractPlatformEndpoint(LeagueClient client) {
        super(client);
        this.tokenSupplier = virtualLeagueClient.get(Authentication.SESSION);
        this.platform = client.getVirtualLeagueClientInstance().getPlatform();
        this.userInformation = client.getVirtualLeagueClientInstance().getUserInformation();
        this.versionSupplier = client.getVirtualLeagueClientInstance().getLocalLeagueFileVersion();
    }
}
