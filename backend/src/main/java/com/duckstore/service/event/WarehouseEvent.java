package com.duckstore.service.event;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import com.duckstore.entity.Duck;
import java.math.BigDecimal;

/** An immutable record of something that happened to the warehouse. */
public record WarehouseEvent(Kind kind, int duckId, Color color, Size size, BigDecimal price, int quantity) {

    public enum Kind { CREATED, MERGED, UPDATED, DELETED }

    public static WarehouseEvent of(Kind kind, Duck duck) {
        return new WarehouseEvent(kind, duck.getId(), duck.getColor(), duck.getSize(), duck.getPrice(), duck.getQuantity());
    }
}
