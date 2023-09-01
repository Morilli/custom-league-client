package com.hawolt.async;

import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.logger.Logger;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Created: 15/08/2023 21:25
 * Author: Twitter @hawolt
 **/

public class LazyLoadedImageComponent extends JPanel implements ResourceConsumer<BufferedImage, byte[]> {
    protected final Dimension dimension;

    protected BufferedImage image;

    protected int x, y;

    public LazyLoadedImageComponent(Dimension dimension) {
        this(null, dimension);
    }

    public LazyLoadedImageComponent(String uri, Dimension dimension) {
        this.dimension = dimension;
        if (uri == null) return;
        ResourceLoader.loadResource(uri, this);
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.fatal("Failed to load {}", o);
        Logger.error(e);
    }

    @Override
    public void consume(Object o, BufferedImage bufferedImage) {
        this.image = Scalr.resize(bufferedImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, dimension.width, dimension.height);
        this.repaint();
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return;
        Dimension bounds = getSize();
        this.x = (bounds.width >> 1) - (image.getWidth() >> 1);
        this.y = (bounds.height >> 1) - (image.getHeight() >> 1);
        g.drawImage(image, x, y, null);
    }
}
