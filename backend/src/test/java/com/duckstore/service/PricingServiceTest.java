package com.duckstore.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.duckstore.common.Destination;
import com.duckstore.common.PackageType;
import com.duckstore.common.ShippingMode;
import com.duckstore.service.pricing.Adjustment;
import com.duckstore.service.pricing.AirShipping;
import com.duckstore.service.pricing.BulkDiscountRule;
import com.duckstore.service.pricing.DestinationRule;
import com.duckstore.service.pricing.LandShipping;
import com.duckstore.service.pricing.PackageRule;
import com.duckstore.service.pricing.PercentageBase;
import com.duckstore.service.pricing.PriceBreakdown;
import com.duckstore.service.pricing.PricingInput;
import com.duckstore.service.pricing.PricingService;
import com.duckstore.service.pricing.SeaShipping;
import com.duckstore.service.pricing.ShippingRule;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PricingServiceTest {

    private static PriceBreakdown price(PercentageBase base, String unitPrice, int quantity, PackageType type,
                                        String country, ShippingMode mode) {
        ShippingRule shipping = new ShippingRule(List.of(new SeaShipping(), new LandShipping(), new AirShipping()));
        PricingService service = new PricingService(
                List.of(new BulkDiscountRule(), new PackageRule(), new DestinationRule(), shipping), base);
        return service.price(new PricingInput(new BigDecimal(unitPrice), quantity, type, Destination.from(country), mode));
    }

    private static PriceBreakdown price(String unitPrice, int quantity, PackageType type, String country,
                                        ShippingMode mode) {
        return price(PercentageBase.RUNNING_TOTAL, unitPrice, quantity, type, country, mode);
    }

    private static boolean has(PriceBreakdown breakdown, String code) {
        return breakdown.adjustments().stream().anyMatch(a -> a.code().equals(code));
    }

    @Test
    void wood_to_usa_by_air_applies_the_rules_in_order() {
        PriceBreakdown b = price("200.00", 10, PackageType.WOOD, "USA", ShippingMode.AIR);

        assertThat(b.subtotal()).isEqualByComparingTo("2000.00");
        assertThat(b.adjustments()).extracting(Adjustment::code)
                .containsExactly("PACKAGE_SURCHARGE", "DESTINATION_SURCHARGE", "AIR_SHIPPING");
        assertThat(b.adjustments()).extracting(a -> a.amount().toPlainString())
                .containsExactly("100.00", "378.00", "300.00");   // 5% of 2000, then 18% of 2100, then 30 x 10
        assertThat(b.total()).isEqualByComparingTo("2778.00");
    }

    @Test
    void bulk_cardboard_india_sea_order() {
        PriceBreakdown b = price("10.00", 150, PackageType.CARDBOARD, "India", ShippingMode.SEA);

        // 1500 - 300 (20%) - 12 (1% of 1200) + 225.72 (19% of 1188) + 400
        assertThat(b.adjustments()).extracting(a -> a.amount().toPlainString())
                .containsExactly("-300.00", "-12.00", "225.72", "400.00");
        assertThat(b.total()).isEqualByComparingTo("1813.72");
    }

    @Test
    void large_air_order_gets_the_bulk_discount_and_the_air_charge_reduction() {
        PriceBreakdown b = price("5.00", 1200, PackageType.PLASTIC, "Bolivia", ShippingMode.AIR);

        // 6000 - 1200 + 480 (10%) + 686.40 (13% of 5280) + 36000 air - 5400 (15% of the air charge)
        assertThat(b.adjustments()).extracting(Adjustment::code).containsExactly(
                "BULK_DISCOUNT", "PACKAGE_SURCHARGE", "DESTINATION_SURCHARGE", "AIR_SHIPPING", "AIR_BULK_REDUCTION");
        assertThat(b.total()).isEqualByComparingTo("36566.40");
    }

    @Test
    void land_shipping_is_ten_per_unit_and_other_countries_pay_fifteen_percent() {
        PriceBreakdown b = price("10.00", 3, PackageType.WOOD, "Narnia", ShippingMode.LAND);

        // 30 + 1.50 (5%) + 4.73 (15% of 31.50, rounded half up) + 30
        assertThat(b.total()).isEqualByComparingTo("66.23");
    }

    @Test
    void the_breakdown_always_adds_up_to_the_total() {
        PriceBreakdown b = price("3.33", 777, PackageType.CARDBOARD, "Bolivia", ShippingMode.LAND);

        BigDecimal sum = b.subtotal();
        for (Adjustment adjustment : b.adjustments()) {
            sum = sum.add(adjustment.amount());
        }
        assertThat(b.total()).isEqualByComparingTo(sum);
    }

    @ParameterizedTest
    @CsvSource({"100,false", "101,true"})
    void bulk_discount_applies_to_more_than_100_units(int quantity, boolean discounted) {
        assertThat(has(price("1.00", quantity, PackageType.WOOD, "France", ShippingMode.SEA), "BULK_DISCOUNT"))
                .isEqualTo(discounted);
    }

    @ParameterizedTest
    @CsvSource({"1000,false", "1001,true"})
    void air_charge_is_reduced_only_above_1000_units(int quantity, boolean reduced) {
        assertThat(has(price("1.00", quantity, PackageType.WOOD, "France", ShippingMode.AIR), "AIR_BULK_REDUCTION"))
                .isEqualTo(reduced);
    }

    @Test
    void subtotal_mode_measures_every_percentage_against_quantity_times_price() {
        PriceBreakdown b = price(PercentageBase.SUBTOTAL, "200.00", 10, PackageType.WOOD, "USA", ShippingMode.AIR);

        assertThat(b.adjustments()).extracting(a -> a.amount().toPlainString())
                .containsExactly("100.00", "360.00", "300.00");   // 5% and 18% of 2000
        assertThat(b.total()).isEqualByComparingTo("2760.00");
    }
}
