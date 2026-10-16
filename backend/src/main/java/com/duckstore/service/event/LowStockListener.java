package com.duckstore.service.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Warns when someone edits a duck down to a low quantity. */
@Component
public class LowStockListener implements WarehouseListener {

    private static final Logger log = LoggerFactory.getLogger(LowStockListener.class);

    private final int threshold;

    public LowStockListener(@Value("${duckstore.warehouse.low-stock-threshold:10}") int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void onEvent(WarehouseEvent e) {
        if (e.kind() == WarehouseEvent.Kind.UPDATED && e.quantity() <= threshold) {
            log.warn("Low stock: duck {} ({} {}) has {} left", e.duckId(), e.color().label(), e.size().label(), e.quantity());
        }
    }
}
