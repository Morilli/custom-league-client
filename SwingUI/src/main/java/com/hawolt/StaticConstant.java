package com.hawolt;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created: 16/08/2023 18:34
 * Author: Twitter @hawolt
 **/

public class StaticConstant {
    public static final Path APPLICATION_CACHE = Paths.get(System.getProperty("java.io.tmpdir")).resolve("swift-rift");
    public static final String USER_AGENT = "Swift-Rift";
    public static final String PROJECT = "Swift-Rift";
}
