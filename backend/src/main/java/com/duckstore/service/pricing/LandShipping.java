package com.duckstore.service.pricing;

import com.duckstore.common.ShippingMode;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/** Rule 11: 10 USD per unit. */
@Component
public class LandShipping implements ShippingChargeStrategy {

    static final BigDecimal PER_UNIT = new BigDecimal("10");

    @Override
    public ShippingMode mode() {
        return ShippingMode.LAND;
    }

    @Override
    public List<Adjustment> charges(int quantity) {
        return List.of(Adjustment.increment("LAND_SHIPPING", "Land shipping: 10 USD per unit",
                PER_UNIT.multiply(BigDecimal.valueOf(quantity))));
    }
}
