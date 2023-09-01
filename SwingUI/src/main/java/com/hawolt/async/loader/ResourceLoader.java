package com.hawolt.async.loader;

import com.hawolt.StaticConstant;
import com.hawolt.async.ExecutorManager;
import com.hawolt.client.cache.ExceptionalSupplier;
import com.hawolt.cryptography.MD5;
import com.hawolt.generic.data.Unsafe;
import com.hawolt.http.layer.IResponse;
import com.hawolt.http.layer.impl.OkHttpResponse;
import com.hawolt.io.Core;
import com.hawolt.io.RunLevel;
import com.hawolt.logger.Logger;
import okhttp3.Request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created: 16/08/2023 18:21
 * Author: Twitter @hawolt
 **/

public class ResourceLoader {
    private static final Path directory = StaticConstant.APPLICATION_CACHE.resolve("assets");
    private static final ExecutorService service = ExecutorManager.registerService(
            "resource-loader-jobs",
            Executors.newFixedThreadPool(8)
    );

    private static final Map<String, List<ResourceConsumer<?, byte[]>>> pending = new HashMap<>();
    private static final LinkedList<Runnable> queue = new LinkedList<>();
    private static final Map<String, byte[]> cache = new HashMap<>();
    private static final Object lock = new Object();

    static {
        ScheduledExecutorService executor = ExecutorManager.getScheduledService("cache-loader");
        executor.execute(() -> {
            File directory = ResourceLoader.directory.toFile();
            if (!directory.exists()) return;
            File[] assets = directory.listFiles();
            if (assets == null) return;
            for (File file : assets) {
                try {
                    Logger.debug("FROM CACHE {}", file.getName());
                    cache.put(file.getName(), Files.readAllBytes(file.toPath()));
                } catch (IOException e) {
                    Logger.error("Failed to load file '{}' from local cache", file.getName());
                }
            }
        });
        executor.shutdown();
        ScheduledExecutorService scheduler = ExecutorManager.getScheduledService("resource-loader");
        scheduler.scheduleWithFixedDelay(() -> {
            synchronized (lock) {
                if (queue.isEmpty()) return;
                service.execute(queue.remove(0));
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    private static <T> T convert(Object in) {
        return Unsafe.cast(in);
    }

    private static void load(String path, boolean priority, ResourceConsumer<?, byte[]> consumer, Runnable runnable) {
        String hash = MD5.hash(path);
        if (cache.containsKey(hash)) {
            try {
                consumer.consume(path, Unsafe.cast(consumer.transform(cache.get(hash))));
            } catch (Exception e) {
                consumer.onException(path, e);
            }
        } else if (pending.containsKey(hash)) {
            pending.get(hash).add(consumer);
        } else {
            pending.put(hash, new ArrayList<>());
            pending.get(hash).add(consumer);
            synchronized (lock) {
                queue.add(priority ? 0 : queue.size(), runnable);
            }
        }
    }

    public static void loadResource(String uri, ResourceConsumer<?, byte[]> consumer) {
        loadResource(uri, false, consumer);
    }

    public static void loadResource(String uri, ExceptionalSupplier<byte[]> supplier,
                                    boolean priority, ResourceConsumer<?, byte[]> consumer) {
        synchronized (lock) {
            queue.add(priority ? 0 : queue.size(), () -> load(uri, priority, consumer, () -> {
                try {
                    handleConsumption(uri, supplier.get());
                } catch (Exception e) {
                    handleError(uri, e);
                }
            }));
        }
    }

    public static void loadResource(String uri, boolean priority, ResourceConsumer<?, byte[]> consumer) {
        synchronized (lock) {
            queue.add(priority ? 0 : queue.size(), () -> load(uri, priority, consumer, () -> {
                Request request = new Request.Builder()
                        .url(uri)
                        .header("User-Agent", StaticConstant.USER_AGENT)
                        .get()
                        .build();
                try {
                    IResponse response = OkHttpResponse.from(request);
                    handleConsumption(uri, response.response());
                } catch (IOException e) {
                    handleError(uri, e);
                }
            }));
        }
    }

    public static void loadLocalResource(String name, ResourceConsumer<?, byte[]> consumer) {
        loadLocalResource(name, false, consumer);
    }

    public static void loadLocalResource(String name, boolean priority, ResourceConsumer<?, byte[]> consumer) {
        synchronized (lock) {
            queue.add(priority ? 0 : queue.size(), () -> load(name, priority, consumer, () -> {
                try (InputStream stream = RunLevel.get(name)) {
                    handleConsumption(name, Core.read(stream).toByteArray());
                } catch (IOException e) {
                    handleError(name, e);
                }
            }));
        }
    }

    private static void writeToCache(String o, String hash, byte[] b) {
        cache.put(hash, b);
        service.execute(() -> {
            try {
                if (!directory.toFile().exists()) Files.createDirectories(directory);
                Path target = directory.resolve(hash);
                if (target.toFile().exists()) return;
                Files.write(
                        target,
                        b,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
                Logger.debug("stored '{}' in local cache as {}", o, hash);
            } catch (IOException e) {
                Logger.error(e);
            }
        });
    }

    private static void handleConsumption(String o, byte[] b) {
        String hash = MD5.hash(o);
        writeToCache(o, hash, b);
        if (!pending.containsKey(hash)) Logger.error("attempt to load unknown value '{}' from cache", o);
        List<ResourceConsumer<?, byte[]>> list = new ArrayList<>(pending.get(hash));
        for (ResourceConsumer<?, byte[]> consumer : list) {
            try {
                consumer.consume(o, convert(consumer.transform(b)));
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }

    private static void handleError(String o, Exception e) {
        List<ResourceConsumer<?, byte[]>> list = new ArrayList<>(pending.get(o));
        for (ResourceConsumer<?, byte[]> consumer : list) {
            consumer.onException(o, e);
        }
    }
}
