package com.hawolt.client.settings;

import com.hawolt.logger.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public abstract class SettingsService<T extends SettingsObject>{
    protected T settings;
    protected Path path;

    public T getSettings() {
        return settings;
    }

    public void writeSettingsFile() throws IOException {
        try {
            Files.createDirectories(SettingsConstant.LOCAL_SETTINGS_DIRECTORY);
            Files.writeString(
                    path,
                    settings.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            Logger.error(e);
            throw e;
        }
    }

    public void readSettingsFile() throws IOException {
        try {
            JSONObject file = new JSONObject(new String(Files.readAllBytes(path)));
            settings.copyData(file);
        } catch (IOException e) {
            throw e;
        }
    }

    public void deleteSettingsFile() throws IOException {
        if (path.toFile().exists()) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                Logger.error(e);
                throw e;
            }
        }
    }
}
