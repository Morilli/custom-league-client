package com.hawolt.settings;

import com.hawolt.StaticConstant;
import com.hawolt.logger.Logger;
import com.hawolt.virtual.misc.DynamicObject;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Created: 28/08/2023 18:55
 * Author: Twitter @hawolt
 **/

public class SettingManager implements SettingService {
    private final ClientSettings client;
    private UserSettings player;
    private String username;

    public SettingManager() {
        this.client = new ClientSettings(load(SettingType.CLIENT));
    }

    @Override
    public UserSettings set(String username) {
        this.username = username;
        this.player = new UserSettings(load(SettingType.PLAYER));
        return player;
    }

    @Override
    public ClientSettings getClientSettings() {
        return client;
    }

    @Override
    public void write(SettingType type, String name, Object o) {
        DynamicObject object = switch (type) {
            case CLIENT -> client;
            case PLAYER -> player;
        };
        object.put(name, o);
        switch (type) {
            case CLIENT -> write(SettingType.CLIENT);
            case PLAYER -> write(SettingType.PLAYER);
        }
    }

    @Override
    public UserSettings getUserSettings() {
        return player;
    }

    private JSONObject load(SettingType type) {
        Path path = switch (type) {
            case CLIENT -> StaticConstant.APPLICATION_SETTINGS.resolve(StaticConstant.CLIENT_SETTING_fILE);
            case PLAYER ->
                    StaticConstant.APPLICATION_SETTINGS.resolve(username).resolve(StaticConstant.PLAYER_SETTING_fILE);
        };
        try {
            return new JSONObject(new String(Files.readAllBytes(path)));
        } catch (IOException e) {
            Logger.info("Unable to locate {}-setting file", type.name().toLowerCase(), path.toFile());
        }
        return new JSONObject();
    }

    private void write(SettingType type) {
        Path path = switch (type) {
            case CLIENT -> StaticConstant.APPLICATION_SETTINGS.resolve(StaticConstant.CLIENT_SETTING_fILE);
            case PLAYER ->
                    StaticConstant.APPLICATION_SETTINGS.resolve(username).resolve(StaticConstant.PLAYER_SETTING_fILE);
        };
        try {
            Files.createDirectories(path.getParent());
            byte[] content = switch (type) {
                case CLIENT -> client.toString().getBytes(StandardCharsets.UTF_8);
                case PLAYER -> player.toString().getBytes(StandardCharsets.UTF_8);
            };
            Files.write(
                    path,
                    content,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            Logger.info("Unable to create directory {}", path.toFile());
        }
    }
}
