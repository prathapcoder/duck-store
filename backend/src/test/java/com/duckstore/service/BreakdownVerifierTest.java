package com.duckstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.duckstore.common.Destination;
import com.duckstore.common.PackageType;
import com.duckstore.common.ShippingMode;
import com.duckstore.service.pricing.Adjustment;
import com.duckstore.service.pricing.BreakdownVerifier;
import com.duckstore.service.pricing.PriceBreakdown;
import com.duckstore.service.pricing.PriceCalculator;
import com.duckstore.service.pricing.PricingInput;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The decorator is tested with hand-made calculators, one that is right and two that are broken. */
class BreakdownVerifierTest {

    private static final PricingInput INPUT =
            new PricingInput(new BigDecimal("10.00"), 1, PackageType.WOOD, Destination.USA, ShippingMode.SEA);

    private static PriceBreakdown breakdown(String total, Adjustment... lines) {
        return new PriceBreakdown(new BigDecimal("10.00"), 1, new BigDecimal("10.00"), List.of(lines), new BigDecimal(total));
    }

    @Test
    void passes_a_consistent_breakdown_through_unchanged() {
        PriceBreakdown good = breakdown("10.50", Adjustment.increment("X", "x", new BigDecimal("0.50")));

        assertThat(new BreakdownVerifier(input -> good).price(INPUT)).isSameAs(good);
    }

    @Test
    void rejects_lines_that_do_not_add_up_to_the_total() {
        PriceCalculator broken = input -> breakdown("10.51", Adjustment.increment("X", "x", new BigDecimal("0.50")));

        assertThatThrownBy(() -> new BreakdownVerifier(broken).price(INPUT))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("does not add up");
    }

    @Test
    void rejects_a_total_that_is_zero_or_negative() {
        PriceCalculator free = input -> breakdown("0.00", Adjustment.discount("X", "x", new BigDecimal("10.00")));

        assertThatThrownBy(() -> new BreakdownVerifier(free).price(INPUT))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("must be positive");
    }
}
