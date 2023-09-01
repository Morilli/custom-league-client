package com.hawolt.client.settings.login;

import com.hawolt.client.settings.SettingsConstant;
import com.hawolt.client.settings.SettingsService;
import com.hawolt.logger.Logger;

import java.io.IOException;

public class LoginSettingsService extends SettingsService<LoginSettings> {
    private static final LoginSettingsService INSTANCE = new LoginSettingsService();

    private LoginSettingsService() {
        settings = new LoginSettings();
        path = SettingsConstant.LOCAL_SETTINGS_DIRECTORY.resolve(SettingsConstant.LOGIN_SETTINGS_FILE);
    }

    @Override
    public void writeSettingsFile() throws IOException {
        Logger.debug("Storing credentials at {}", path.toFile().getAbsolutePath());
        try {
            super.writeSettingsFile();
        } catch (IOException e) {
            Logger.fatal("Error handling credentials");
            throw (e);
        }
    }

    @Override
    public void readSettingsFile() throws IOException {
        try {
            super.readSettingsFile();
        }
        catch (IOException e) {
            Logger.warn("Failed to read credentials at {}", this.path);
            throw e;
        }
    }

    @Override
    public void deleteSettingsFile() throws IOException {
        try {
            super.deleteSettingsFile();
        } catch (IOException e) {
            Logger.warn("Failed to delete credentials");
            throw e;
        }
    }

    public static LoginSettingsService get() {
        return INSTANCE;
    }

}
