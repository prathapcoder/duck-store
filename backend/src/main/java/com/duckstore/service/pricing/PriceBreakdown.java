package com.duckstore.service.pricing;

import java.math.BigDecimal;
import java.util.List;

public record PriceBreakdown(BigDecimal unitPrice, int quantity, BigDecimal subtotal,
                             List<Adjustment> adjustments, BigDecimal total) {
}
