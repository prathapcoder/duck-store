package com.duckstore.service.pricing;

import com.duckstore.common.ShippingMode;
import com.duckstore.util.Money;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/** Rule 12: 30 USD per unit; for more than 1000 units the air charge is reduced by 15%. */
@Component
public class AirShipping implements ShippingChargeStrategy {

    static final BigDecimal PER_UNIT = new BigDecimal("30");
    static final int BULK_THRESHOLD_UNITS = 1000;
    static final int BULK_REDUCTION_PERCENT = 15;

    @Override
    public ShippingMode mode() {
        return ShippingMode.AIR;
    }

    @Override
    public List<Adjustment> charges(int quantity) {
        BigDecimal charge = PER_UNIT.multiply(BigDecimal.valueOf(quantity));
        Adjustment shipping = Adjustment.increment("AIR_SHIPPING", "Air shipping: 30 USD per unit", charge);
        if (quantity <= BULK_THRESHOLD_UNITS) {
            return List.of(shipping);
        }
        Adjustment reduction = Adjustment.discount("AIR_BULK_REDUCTION",
                BULK_REDUCTION_PERCENT + "% reduction of the air charge for orders of more than "
                        + BULK_THRESHOLD_UNITS + " units",
                Money.percentOf(charge, BULK_REDUCTION_PERCENT));
        return List.of(shipping, reduction);
    }
}
