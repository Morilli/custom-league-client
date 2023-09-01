package com.hawolt.client.resources.communitydragon;

/**
 * Created: 30/08/2023 19:40
 * Author: Twitter @hawolt
 **/

public interface DataTypeConverter<T, S> {
    S apply(T s) throws Exception;
}
