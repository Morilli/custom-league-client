package com.hawolt.client.settings;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SettingsConstant {
    public static final String PROJECT = "Swift-Rift";
    public static final Path LOCAL_SETTINGS_DIRECTORY = Paths.get(System.getProperty("user.home")).resolve(PROJECT);
    public static final Path LOGIN_SETTINGS_FILE = LOCAL_SETTINGS_DIRECTORY.resolve(".swift-rift-login");
    public static final Path CLIENT_SETTINGS_FILE = LOCAL_SETTINGS_DIRECTORY.resolve(".swift-rift-client");
}
