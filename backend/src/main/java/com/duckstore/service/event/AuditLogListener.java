package com.duckstore.service.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Writes one line per warehouse change. */
@Component
public class AuditLogListener implements WarehouseListener {

    private static final Logger log = LoggerFactory.getLogger(AuditLogListener.class);

    @Override
    public void onEvent(WarehouseEvent e) {
        log.info("warehouse {} duck {}: {} {} at {} USD, quantity {}",
                e.kind(), e.duckId(), e.color().label(), e.size().label(), e.price(), e.quantity());
    }
}
