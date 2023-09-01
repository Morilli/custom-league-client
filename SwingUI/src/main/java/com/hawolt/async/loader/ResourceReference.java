package com.hawolt.async.loader;

import java.awt.image.BufferedImage;

/**
 * Created: 22/08/2023 18:25
 * Author: Twitter @hawolt
 **/

public class ResourceReference {
    private final String name;
    private BufferedImage image;

    public ResourceReference(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}
