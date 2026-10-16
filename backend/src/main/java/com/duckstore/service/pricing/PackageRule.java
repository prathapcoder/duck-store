package com.duckstore.service.pricing;

import com.duckstore.common.PackageType;
import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Rules 3-5: wood +5%, plastic +10%, cardboard -1%. */
@Component
@Order(20)
public class PackageRule extends PercentageRule {

    /** Signed percent: positive is a surcharge, negative a discount. */
    private static final Map<PackageType, Integer> PERCENT = Map.of(
            PackageType.WOOD, 5,
            PackageType.PLASTIC, 10,
            PackageType.CARDBOARD, -1);

    @Override
    protected int signedPercent(PricingInput input) {
        return PERCENT.get(input.packageType());
    }

    @Override
    protected String code(int signedPercent) {
        return signedPercent < 0 ? "PACKAGE_DISCOUNT" : "PACKAGE_SURCHARGE";
    }

    @Override
    protected String description(PricingInput input, int percent) {
        String kind = PERCENT.get(input.packageType()) < 0 ? "discount" : "surcharge";
        return percent + "% " + kind + " for " + input.packageType().label().toLowerCase() + " package";
    }
}
