package com.hawolt.ui.store;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.store.objects.StoreItem;
import com.hawolt.client.resources.ledge.store.objects.StoreSortOrder;
import com.hawolt.client.resources.ledge.store.objects.StoreSortProperty;
import com.hawolt.logger.Logger;
import com.hawolt.ui.impl.Debouncer;
import com.hawolt.ui.impl.JHintTextField;
import com.hawolt.util.AudioEngine;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.panel.ChildUIComponent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created: 09/08/2023 18:45
 * Author: Twitter @hawolt
 **/

public class StorePage extends ChildUIComponent implements IStorePage {

    private final Map<Long, StoreElement> map = new HashMap<>();
    private final LeagueClient client;
    private final ChildUIComponent grid;
    private final List<Long> owned;
    private final StoreElementComparator comparator;
    private final Debouncer debouncer = new Debouncer();
    private String filter = "";

    public StorePage(LeagueClient client, List<Long> owned, StoreSortProperty... properties) {
        super(new BorderLayout(0, 5));
        this.client = client;
        this.owned = owned;
        ChildUIComponent component = new ChildUIComponent(new BorderLayout());
        grid = new ChildUIComponent(new GridLayout(0, 5, 5, 5));
        add(component, BorderLayout.NORTH);
        component.add(grid, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(component);
        //TODO revisit this is good
        /*scrollPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Rectangle visibleRect = grid.getVisibleRect();
                for (Component child : grid.getComponents()) {
                    Rectangle childBounds = child.getBounds();
                    if (childBounds.intersects(visibleRect)) {
                        System.out.println("INTERSECT");
                    } else {
                        System.out.println("NO INTERSECT");
                    }
                }
            }
        });
        */
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        setBorder(new EmptyBorder(5, 5, 5, 0));

        comparator = new StoreElementComparator(properties.length > 0 ? properties[0] : null, StoreSortOrder.ASCENDING);
        JPanel inputPanel = createInputPanel(properties);
        this.add(inputPanel, BorderLayout.NORTH);
    }

    @NotNull
    private JPanel createInputPanel(StoreSortProperty[] properties) {
        JComboBox<StoreSortOption> sortBox = createStoreSortOptionJComboBox(properties);

        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(ColorPalette.BACKGROUND_COLOR);
        inputPanel.setLayout(new GridLayout(1, 2, 5, 0));
        inputPanel.add(sortBox);
        JHintTextField search = new JHintTextField("Search...");

        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filter = search.getText().toLowerCase();
                debouncer.debounce("searchField", () -> updateElements(), 200, TimeUnit.MILLISECONDS);
            }
        });

        inputPanel.add(search);
        return inputPanel;
    }

    @NotNull
    private JComboBox<StoreSortOption> createStoreSortOptionJComboBox(StoreSortProperty[] properties) {
        JComboBox<StoreSortOption> sortBox = new JComboBox<>();
        for (StoreSortProperty property : properties) {
            sortBox.addItem(new StoreSortOption(property, StoreSortOrder.ASCENDING));
            sortBox.addItem(new StoreSortOption(property, StoreSortOrder.DESCENDING));
        }
        sortBox.addItemListener(listener -> {
            AudioEngine.play("air_button_press_1.wav");
            StoreSortOption option = sortBox.getItemAt(sortBox.getSelectedIndex());
            comparator.setProperty(option.property());
            comparator.setOrder(option.order());
            updateElements();
        });
        return sortBox;
    }

    public ChildUIComponent getGrid() {
        return grid;
    }

    public void append(StoreItem item) {
        append(Collections.singletonList(item));
    }

    public void append(List<StoreItem> items) {
        try {
            for (StoreItem item : items) {
                if (owned.contains(item.getItemId())) continue;
                JSONObject object = item.asJSON();
                long itemId = object.getLong("itemId");
                StoreElement element = new StoreElement(client, this, item);
                map.put(itemId, element);
                grid.add(element);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        updateElements();
    }

    @Override
    public void removeStoreElement(StoreElement component) {
        grid.remove(component);
        map.remove(component.getItem().getItemId());
        updateElements();
    }

    public void updateElements() {
        grid.removeAll();
        map.values()
                .stream()
                .sorted(this.comparator)
                .filter(champion -> champion.getItem().getName().toLowerCase().contains(filter))
                .forEach(this.grid::add);
        revalidate();
        repaint();
    }
}
