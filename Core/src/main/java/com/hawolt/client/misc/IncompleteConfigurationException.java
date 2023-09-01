package com.hawolt.client.misc;

/**
 * Created: 27/07/2023 21:37
 * Author: Twitter @hawolt
 **/

public class IncompleteConfigurationException extends Exception {
    public IncompleteConfigurationException() {
        super("one or multiple values are null");
    }
}
