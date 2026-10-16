package com.duckstore.service.pricing;

import com.duckstore.util.Money;
import java.math.BigDecimal;
import java.util.List;

/**
 * Template Method: the algorithm of every "x% of the total cost" rule is fixed here once (skip when the
 * percent is 0, compute the amount from the percentage base, label it a discount or an increment).
 * Subclasses only say which percent applies and how to name the line.
 */
public abstract class PercentageRule implements PricingRule {

    @Override
    public final List<Adjustment> evaluate(PricingContext context) {
        PricingInput input = context.input();
        int percent = signedPercent(input);
        if (percent == 0) {
            return List.of();
        }
        int magnitude = Math.abs(percent);
        BigDecimal amount = Money.percentOf(context.percentageBase(), magnitude);
        String code = code(percent);
        String description = description(input, magnitude);
        return List.of(percent < 0
                ? Adjustment.discount(code, description, amount)
                : Adjustment.increment(code, description, amount));
    }

    /** Positive for a surcharge, negative for a discount, 0 when the rule does not apply. */
    protected abstract int signedPercent(PricingInput input);

    protected abstract String code(int signedPercent);

    protected abstract String description(PricingInput input, int percent);
}
