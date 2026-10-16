package com.duckstore.dto;

import com.duckstore.common.Color;
import com.duckstore.common.Limits;
import com.duckstore.common.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Only price and quantity can be edited. A client may echo the whole duck back: color and size are
 * accepted when they match the stored values and rejected when they differ (see DuckService).
 */
public record DuckUpdateRequest(
        Color color,
        Size size,
        @NotNull(message = "price is required")
        @DecimalMin(value = "0.01", message = "price must be at least 0.01")
        @Digits(integer = 10, fraction = 2, message = "price must have at most 2 decimal places")
        BigDecimal price,
        @NotNull(message = "quantity is required")
        @Min(value = 0, message = "quantity must not be negative")
        @Max(value = Limits.MAX_QUANTITY, message = "quantity is too large")
        Integer quantity) {
}
