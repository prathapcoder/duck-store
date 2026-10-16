package com.duckstore.service.pricing;

import java.math.BigDecimal;

/**
 * Decorator: wraps any {@link PriceCalculator} and checks the invariants the brief implies, so a future
 * rule that breaks them fails loudly instead of returning a wrong price: the itemised lines must add up to
 * the total to the cent, and an order can never cost zero or less.
 */
public class BreakdownVerifier implements PriceCalculator {

    private final PriceCalculator delegate;

    public BreakdownVerifier(PriceCalculator delegate) {
        this.delegate = delegate;
    }

    @Override
    public PriceBreakdown price(PricingInput input) {
        PriceBreakdown breakdown = delegate.price(input);
        BigDecimal sum = breakdown.adjustments().stream()
                .map(Adjustment::amount)
                .reduce(breakdown.subtotal(), BigDecimal::add);
        if (sum.compareTo(breakdown.total()) != 0) {
            throw new IllegalStateException("Price breakdown does not add up: lines give " + sum
                    + " but the total is " + breakdown.total());
        }
        if (breakdown.total().signum() <= 0) {
            throw new IllegalStateException("Order total must be positive but was " + breakdown.total());
        }
        return breakdown;
    }
}
