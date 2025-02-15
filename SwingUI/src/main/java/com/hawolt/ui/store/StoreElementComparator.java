package com.hawolt.ui.store;

import com.hawolt.client.resources.ledge.store.objects.StoreItemComparator;
import com.hawolt.client.resources.ledge.store.objects.StoreSortOrder;
import com.hawolt.client.resources.ledge.store.objects.StoreSortProperty;

import java.util.Comparator;

/**
 * A wrapper around {@link StoreItemComparator} that sorts {@link StoreElement}s by their underlying item.
 */

public class StoreElementComparator implements Comparator<StoreElement> {

    private final StoreItemComparator underlying;

    public StoreElementComparator(StoreSortProperty property, StoreSortOrder order) {
        this.underlying = new StoreItemComparator(property, order);
    }

    public void setProperty(StoreSortProperty property) {
        this.underlying.setProperty(property);
    }

    public void setOrder(StoreSortOrder order) {
        this.underlying.setOrder(order);
    }

    @Override
    public int compare(StoreElement o1, StoreElement o2) {
        return this.underlying.compare(o1.getItem(), o2.getItem());
    }
}
