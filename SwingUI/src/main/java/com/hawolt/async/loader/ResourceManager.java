package com.hawolt.async.loader;

import com.hawolt.client.resources.communitydragon.DataTypeConverter;
import com.hawolt.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created: 30/08/2023 19:06
 * Author: Twitter @hawolt
 **/

public class ResourceManager<T> implements ResourceConsumer<T, byte[]> {
    private final Map<Object, Consumer<T>> map = new HashMap<>();
    private final DataTypeConverter<byte[], T> function;

    public ResourceManager(DataTypeConverter<byte[], T> function) {
        this.function = function;
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.warn("Failed to load {}", o);
    }

    @Override
    public void consume(Object o, T t) {
        this.map.get(o).accept(t);
    }

    @Override
    public T transform(byte[] bytes) throws Exception {
        return function.apply(bytes);
    }

    public void load(String link, Consumer<T> consumer) {
        this.map.put(link, consumer);
        ResourceLoader.loadResource(link, this);
    }
}
