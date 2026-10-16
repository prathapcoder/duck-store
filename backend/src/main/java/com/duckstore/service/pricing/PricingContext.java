package com.duckstore.service.pricing;

import com.duckstore.util.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The state a pricing run carries from rule to rule, and the Builder of the immutable {@link PriceBreakdown}:
 * rules add adjustments, {@link #build()} produces the result.
 */
public final class PricingContext {

    private final PricingInput input;
    private final PercentageBase percentageBase;
    private final BigDecimal subtotal;
    private final List<Adjustment> adjustments = new ArrayList<>();
    private BigDecimal running;

    public PricingContext(PricingInput input, PercentageBase percentageBase) {
        this.input = input;
        this.percentageBase = percentageBase;
        this.subtotal = Money.round(input.unitPrice().multiply(BigDecimal.valueOf(input.quantity())));
        this.running = subtotal;
    }

    public PricingInput input() {
        return input;
    }

    public BigDecimal subtotal() {
        return subtotal;
    }

    public BigDecimal running() {
        return running;
    }

    public List<Adjustment> adjustments() {
        return List.copyOf(adjustments);
    }

    /** The amount that a "percent of the total cost" rule is applied to. */
    public BigDecimal percentageBase() {
        return percentageBase == PercentageBase.SUBTOTAL ? subtotal : running;
    }

    public PriceBreakdown build() {
        return new PriceBreakdown(input.unitPrice(), input.quantity(), subtotal, List.copyOf(adjustments), running);
    }

    void record(Adjustment adjustment) {
        adjustments.add(adjustment);
        running = running.add(adjustment.amount());
    }
}
