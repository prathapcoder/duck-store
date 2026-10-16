package com.duckstore.dto;

import com.duckstore.common.Color;
import com.duckstore.common.Limits;
import com.duckstore.common.ShippingMode;
import com.duckstore.common.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotNull(message = "color is required") Color color,
        @NotNull(message = "size is required") Size size,
        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1")
        @Max(value = Limits.MAX_QUANTITY, message = "quantity is too large")
        Integer quantity,
        @NotBlank(message = "destinationCountry is required") String destinationCountry,
        @NotNull(message = "shippingMode is required") ShippingMode shippingMode) {
}
