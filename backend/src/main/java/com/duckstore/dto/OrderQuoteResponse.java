package com.duckstore.dto;

import com.duckstore.common.PackageType;
import com.duckstore.common.Protection;
import com.duckstore.service.OrderQuote;
import com.duckstore.service.pricing.Adjustment;
import java.math.BigDecimal;
import java.util.List;

public record OrderQuoteResponse(int duckId, int quantity, BigDecimal unitPrice, PackageType packageType,
                                 List<Protection> protections, BigDecimal subtotal,
                                 List<AdjustmentResponse> adjustments, BigDecimal total, String currency) {

    public record AdjustmentResponse(String code, String description, String type, BigDecimal amount) {
        static AdjustmentResponse from(Adjustment adjustment) {
            return new AdjustmentResponse(adjustment.code(), adjustment.description(),
                    adjustment.amount().signum() < 0 ? "DISCOUNT" : "INCREMENT", adjustment.amount());
        }
    }

    public static OrderQuoteResponse from(OrderQuote quote) {
        return new OrderQuoteResponse(quote.duckId(), quote.price().quantity(), quote.price().unitPrice(),
                quote.packaging().packageType(), quote.packaging().protections(), quote.price().subtotal(),
                quote.price().adjustments().stream().map(AdjustmentResponse::from).toList(),
                quote.price().total(), "USD");
    }
}
