package com.hawolt.ui.store;

import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.client.resources.ledge.store.objects.InventoryType;
import com.hawolt.client.resources.ledge.store.objects.StoreItem;
import com.hawolt.logger.Logger;
import org.imgscalr.Scalr;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 09/08/2023 20:09
 * Author: Twitter @hawolt
 **/

public class StoreImage extends JComponent implements IStoreImage, ResourceConsumer<BufferedImage, byte[]> {
    private final StoreItem item;
    private BufferedImage image;

    public StoreImage(StoreItem item) {
        this.item = item;
        ResourceLoader.loadResource(getImageURL(item.getInventoryType(), item.getItemId()), this);
    }

    @Override
    public String getImageURL(InventoryType type, long itemId) {
        switch (type) {
            case CHAMPION_SKIN -> {
                JSONObject raw = item.asJSON();
                JSONArray requirements = raw.getJSONArray("itemRequirements");
                Map<String, Long> map = new HashMap<>();
                for (int i = 0; i < requirements.length(); i++) {
                    JSONObject requirement = requirements.getJSONObject(i);
                    map.put(requirement.getString("inventoryType"), requirement.getLong("itemId"));
                }
                long skinId = map.getOrDefault(InventoryType.CHAMPION_SKIN.name(), itemId);
                return String.format(
                        "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-splashes/%s/%s.jpg",
                        map.get(InventoryType.CHAMPION.name()),
                        skinId
                );
            }
            default -> {
                return String.format(
                        "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-splashes/%s/%s.jpg",
                        item.getItemId(),
                        item.getItemId() * 1000
                );
            }
        }
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            Dimension dimension = getSize();
            int x = (dimension.width >> 1) - (image.getWidth() >> 1);
            int y = (dimension.height >> 1) - (image.getHeight() >> 1);
            g.drawImage(image, x, y, null);
        }
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.fatal("Failed to load resource {}", o);
        Logger.error(e);
    }

    @Override
    public void consume(Object o, BufferedImage image) {
        this.image = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, 200, 260);
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
