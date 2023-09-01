package com.hawolt.ui.store;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheType;
import com.hawolt.client.cache.CachedValueLoader;
import com.hawolt.client.resources.ledge.store.objects.InventoryType;
import com.hawolt.client.resources.ledge.store.objects.StoreItem;
import com.hawolt.client.resources.purchasewidget.CurrencyType;
import com.hawolt.client.resources.purchasewidget.PurchaseWidget;
import com.hawolt.util.AudioEngine;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.panel.ChildUIComponent;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created: 09/08/2023 19:01
 * Author: Twitter @hawolt
 **/

public class StoreElement extends ChildUIComponent implements IStoreElement {
    private final List<StoreButton> buttons = new ArrayList<>();
    private final LeagueClient client;
    private StoreImage image;
    private final StoreItem item;
    private final IStorePage page;

    public StoreElement(LeagueClient client, IStorePage page, StoreItem item) {
        super(new BorderLayout());
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.add(image = new StoreImage(item), BorderLayout.CENTER);
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.setPreferredSize(new Dimension(150, 300));
        this.client = client;
        this.item = item;
        this.page = page;
        this.build();
    }

    private void build() {
        if (item.isBlueEssencePurchaseAvailable() && item.getBlueEssenceCost() > 0)
            buttons.add(new StoreButton(this, CurrencyType.IP, item.getBlueEssenceCost()));
        if (item.isRiotPointPurchaseAvailable() && item.getRiotPointCost() > 0)
            buttons.add(new StoreButton(this, CurrencyType.RP, item.getRiotPointCost()));
        ChildUIComponent mainComponent = new ChildUIComponent(new GridLayout(0, 1, 0, 0));
        ChildUIComponent nameComponent = new ChildUIComponent(new GridLayout(0, 1, 0, 0));
        JLabel name = new JLabel(this.item.getName());
        name.setHorizontalAlignment(SwingConstants.CENTER);
        name.setForeground(Color.WHITE);
        nameComponent.setBackground(Color.GRAY);
        nameComponent.add(name);
        mainComponent.add(nameComponent);
        ChildUIComponent buttonComponent = new ChildUIComponent(new GridLayout(0, buttons.isEmpty() ? 1 : buttons.size(), 0, 0));
        for (StoreButton button : buttons) {
            buttonComponent.add(button);
        }
        buttonComponent.setBackground(Color.DARK_GRAY);
        mainComponent.add(buttonComponent);
        add(mainComponent, BorderLayout.SOUTH);
        revalidate();
    }

    public StoreItem getItem() {
        return item;
    }

    public StoreImage getImage() {
        return image;
    }

    @Override
    public void purchase(CurrencyType currencyType, long price) {
        AudioEngine.play("air_button_press_1.wav");
        LeagueClientUI.service.execute(() -> {
            try {
                PurchaseWidget widget = client.getPurchaseWidget();
                JSONObject response = new JSONObject(widget.purchase(currencyType, InventoryType.CHAMPION, item.getItemId(), price));
                if (response.has("errorCode")) {
                    AudioEngine.play("friend_logout.wav");
                } else {
                    AudioEngine.play("openstore.wav");
                    page.removeStoreElement(this);
                    LeagueClientUI.service.execute(
                            new CachedValueLoader<>(
                                    CacheType.INVENTORY_TOKEN,
                                    () -> client.getLedge().getInventoryService().getInventoryToken(),
                                    client
                            )
                    );
                    revalidate();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}