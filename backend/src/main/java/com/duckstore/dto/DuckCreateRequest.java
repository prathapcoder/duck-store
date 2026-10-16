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

public record DuckCreateRequest(
        @NotNull(message = "color is required") Color color,
        @NotNull(message = "size is required") Size size,
        @NotNull(message = "price is required")
        @DecimalMin(value = "0.01", message = "price must be at least 0.01")
        @Digits(integer = 10, fraction = 2, message = "price must have at most 2 decimal places")
        BigDecimal price,
        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1")
        @Max(value = Limits.MAX_QUANTITY, message = "quantity is too large")
        Integer quantity) {
}
