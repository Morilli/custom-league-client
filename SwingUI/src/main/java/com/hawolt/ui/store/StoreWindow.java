package com.hawolt.ui.store;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.store.StoreLedge;
import com.hawolt.client.resources.ledge.store.objects.InventoryType;
import com.hawolt.client.resources.ledge.store.objects.StoreItem;
import com.hawolt.client.resources.ledge.store.objects.StoreSortProperty;
import com.hawolt.logger.Logger;
import com.hawolt.util.panel.ChildUIComponent;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Created: 09/08/2023 17:44
 * Author: Twitter @hawolt
 **/

public class StoreWindow extends ChildUIComponent implements Runnable {
    private final Map<InventoryType, List<StoreItem>> cache = new HashMap<>();
    private final LeagueClient client;

    private final JTabbedPane pane;

    public StoreWindow(LeagueClient client) {
        super(new BorderLayout());
        this.client = client;
        this.setBackground(Color.GRAY);
        pane = new JTabbedPane();
        try {
            String jwt = client.getLedge().getInventoryService().getInventoryToken();
            JSONObject object = new JSONObject(new String(Base64.getDecoder().decode(jwt.split("\\.")[1])));
            JSONObject items = object.getJSONObject("items");
            JSONArray champions = items.getJSONArray("CHAMPION");
            List<Long> list = champions.toList()
                    .stream()
                    .map(Object::toString)
                    .map(Long::parseLong)
                    .toList();
            pane.addTab(
                    InventoryType.CHAMPION.name(),
                    new StorePage(
                            client,
                            list,
                            StoreSortProperty.values()
                    )
            );
            //  pane.addTab(InventoryType.CHAMPION_SKIN.name(), new StorePage(client));
        } catch (Exception e) {
            Logger.error(e);
        }
        //    pane.addTab(InventoryType.CHAMPION_SKIN.name(), new StorePage());
        add(pane, BorderLayout.CENTER);

        LeagueClientUI.service.execute(this);
    }

    public StorePage getTabByName(String name) {
        for (int i = 0; i < pane.getTabCount(); i++) {
            if (pane.getTitleAt(i).equals(name)) {
                return (StorePage) pane.getComponentAt(i);
            }
        }
        return null;
    }

    @Override
    public void run() {
        StoreLedge store = client.getLedge().getStore();
        try {
            List<StoreItem> list = store.catalogV1();
            Map<InventoryType, StorePage> map = new HashMap<>();
            Map<InventoryType, List<StoreItem>> matches = new HashMap<>();
            for (StoreItem item : list) {
                InventoryType type = item.getInventoryType();
                if (!map.containsKey(type)) map.put(type, getTabByName(type.name()));
                StorePage page = map.get(type);
                if (page == null) continue;
                if (!cache.containsKey(type)) cache.put(type, new ArrayList<>());
                cache.get(type).add(item);
                if (!matches.containsKey(type)) matches.put(type, new ArrayList<>());
                matches.get(type).add(item);
            }
            for (InventoryType type : matches.keySet()) {
                StorePage page = map.get(type);
                if (page == null) continue;
                page.append(matches.get(type));
            }
            revalidate();
            repaint();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
