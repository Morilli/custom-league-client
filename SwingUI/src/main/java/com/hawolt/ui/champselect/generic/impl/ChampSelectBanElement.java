package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.async.LazyLoadedImageComponent;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.ui.champselect.util.ActionObject;
import com.hawolt.util.ColorPalette;

import java.awt.*;

/**
 * Created: 31/08/2023 21:17
 * Author: Twitter @hawolt
 **/

public class ChampSelectBanElement extends LazyLoadedImageComponent {
    private final static String preset = "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-icons/%s.png";
    private boolean completed;
    private int championId;

    public ChampSelectBanElement(Dimension dimension) {
        super(dimension);
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
    }

    public void update(ActionObject object) {
        if (completed || object.getChampionId() == 0) return;
        this.championId = object.getChampionId();
        this.completed = object.isCompleted();
        this.update(object.getChampionId());
    }

    public void update(int championId) {
        ResourceLoader.loadResource(String.format(preset, championId), this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (completed || image == null || championId <= 0) return;
        Color infused = new Color((Color.WHITE.getRGB() & 0xFFFFFF) | (0x7F << 24), true);
        g.setColor(infused);
        g.fillRect(x, y, dimension.width, dimension.height);
    }
}
