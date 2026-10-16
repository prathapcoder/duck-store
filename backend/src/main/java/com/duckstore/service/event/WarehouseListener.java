package com.duckstore.service.event;

/** Observer: reacts to warehouse events. Add a behaviour by adding a listener; the use cases never change. */
public interface WarehouseListener {

    void onEvent(WarehouseEvent event);
}
