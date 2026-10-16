package com.duckstore.service.pricing;

import com.duckstore.common.ShippingMode;
import com.duckstore.util.ModeRegistry;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Rules 10-12: flat or per-unit shipping charges. This rule runs last, so the charges are never
 * multiplied by the percentage rules. It only delegates; each mode's charge lives in its own strategy.
 */
@Component
@Order(40)
public class ShippingRule implements PricingRule {

    private final ModeRegistry<ShippingMode, ShippingChargeStrategy> strategies;

    public ShippingRule(List<ShippingChargeStrategy> shippingStrategies) {
        this.strategies = new ModeRegistry<>(ShippingMode.class, shippingStrategies,
                ShippingChargeStrategy::mode, "shipping charge strategy");
    }

    @Override
    public List<Adjustment> evaluate(PricingContext context) {
        PricingInput input = context.input();
        return strategies.get(input.shippingMode()).charges(input.quantity());
    }
}
