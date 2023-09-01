package com.hawolt.client.resources.communitydragon;

/**
 * Created: 29/08/2023 20:13
 * Author: Twitter @hawolt
 **/

public interface CommunityDragonSource<T> {
    String getSource();

    T get();
}
