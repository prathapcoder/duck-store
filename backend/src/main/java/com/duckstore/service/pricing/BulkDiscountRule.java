package com.duckstore.service.pricing;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Rule 2: more than 100 units gets a 20% discount. */
@Component
@Order(10)
public class BulkDiscountRule extends PercentageRule {

    static final int THRESHOLD_UNITS = 100;
    static final int DISCOUNT_PERCENT = 20;

    @Override
    protected int signedPercent(PricingInput input) {
        return input.quantity() > THRESHOLD_UNITS ? -DISCOUNT_PERCENT : 0;
    }

    @Override
    protected String code(int signedPercent) {
        return "BULK_DISCOUNT";
    }

    @Override
    protected String description(PricingInput input, int percent) {
        return percent + "% discount for orders of more than " + THRESHOLD_UNITS + " units";
    }
}
