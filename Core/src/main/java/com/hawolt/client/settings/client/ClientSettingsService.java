package com.hawolt.client.settings.client;

import com.hawolt.client.settings.SettingsConstant;
import com.hawolt.logger.Logger;
import com.hawolt.client.settings.SettingsService;

import java.io.IOException;

public class ClientSettingsService extends SettingsService<ClientSettings> {
    private static final ClientSettingsService INSTANCE = new ClientSettingsService();

    private ClientSettingsService() {
        settings = new ClientSettings();
        path = SettingsConstant.LOCAL_SETTINGS_DIRECTORY.resolve(SettingsConstant.CLIENT_SETTINGS_FILE);
    }

    @Override
    public void writeSettingsFile() throws IOException {
        Logger.debug("Storing client settings at {}", path.toFile().getAbsolutePath());
        try {
            super.writeSettingsFile();
        } catch (IOException e) {
            Logger.fatal("Error handling client settings");
            throw (e);
        }
    }

    @Override
    public void readSettingsFile() throws IOException {
        try {
            super.readSettingsFile();
        }
        catch (IOException e) {
            Logger.warn("Failed to read client settings at {}", this.path);
            throw e;
        }
    }

    @Override
    public void deleteSettingsFile() throws IOException {
        try {
            super.deleteSettingsFile();
        } catch (IOException e) {
            Logger.warn("Failed to delete client settings");
            throw e;
        }
    }

    public static ClientSettingsService get() {
        return INSTANCE;
    }

}
