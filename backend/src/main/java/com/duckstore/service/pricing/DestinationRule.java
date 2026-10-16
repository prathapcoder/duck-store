package com.duckstore.service.pricing;

import com.duckstore.common.Destination;
import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Rules 6-9: USA +18%, Bolivia +13%, India +19%, anywhere else +15%. */
@Component
@Order(30)
public class DestinationRule extends PercentageRule {

    private static final Map<Destination, Integer> PERCENT = Map.of(
            Destination.USA, 18,
            Destination.BOLIVIA, 13,
            Destination.INDIA, 19,
            Destination.OTHER, 15);

    @Override
    protected int signedPercent(PricingInput input) {
        return PERCENT.get(input.destination());
    }

    @Override
    protected String code(int signedPercent) {
        return "DESTINATION_SURCHARGE";
    }

    @Override
    protected String description(PricingInput input, int percent) {
        return percent + "% surcharge for destination " + input.destination().label();
    }
}
