package com.duckstore.service.pricing;

import com.duckstore.common.Destination;
import com.duckstore.common.PackageType;
import com.duckstore.common.ShippingMode;
import java.math.BigDecimal;

public record PricingInput(BigDecimal unitPrice, int quantity, PackageType packageType,
                           Destination destination, ShippingMode shippingMode) {
}
