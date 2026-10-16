package com.duckstore.service.pricing;

import com.duckstore.common.ShippingMode;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/** Rule 10: a flat 400 USD. */
@Component
public class SeaShipping implements ShippingChargeStrategy {

    static final BigDecimal FLAT_FEE = new BigDecimal("400");

    @Override
    public ShippingMode mode() {
        return ShippingMode.SEA;
    }

    @Override
    public List<Adjustment> charges(int quantity) {
        return List.of(Adjustment.increment("SEA_SHIPPING", "Sea shipping flat fee", FLAT_FEE));
    }
}
