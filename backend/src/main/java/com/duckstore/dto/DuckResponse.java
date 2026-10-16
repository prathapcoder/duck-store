package com.duckstore.dto;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import com.duckstore.entity.Duck;
import java.math.BigDecimal;

public record DuckResponse(int id, Color color, Size size, BigDecimal price, int quantity) {

    public static DuckResponse from(Duck duck) {
        return new DuckResponse(duck.getId(), duck.getColor(), duck.getSize(), duck.getPrice(), duck.getQuantity());
    }
}
