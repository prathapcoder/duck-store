package com.duckstore.service.pricing;

/** The abstraction the order flow depends on. Implementations can be wrapped (Decorator). */
@FunctionalInterface
public interface PriceCalculator {

    PriceBreakdown price(PricingInput input);
}
