package com.duckstore.service;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import java.math.BigDecimal;

/** An edit request. Color and size are optional echoes of the stored values; they are never applied. */
public record DuckChange(Color color, Size size, BigDecimal price, int quantity) {
}
