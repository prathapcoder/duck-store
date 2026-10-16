package com.duckstore.service.pricing;

import java.util.List;

/** Runs every {@link PricingRule} in order and totals the result. It knows nothing about any single rule. */
public class PricingService implements PriceCalculator {

    private final List<PricingRule> rules;
    private final PercentageBase percentageBase;

    public PricingService(List<PricingRule> rules, PercentageBase percentageBase) {
        this.rules = rules;
        this.percentageBase = percentageBase;
    }

    @Override
    public PriceBreakdown price(PricingInput input) {
        PricingContext context = new PricingContext(input, percentageBase);
        for (PricingRule rule : rules) {
            for (Adjustment adjustment : rule.evaluate(context)) {
                context.record(adjustment);
            }
        }
        return context.build();
    }
}
