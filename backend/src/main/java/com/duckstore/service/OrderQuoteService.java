package com.duckstore.service;

import com.duckstore.common.Destination;
import com.duckstore.entity.Duck;
import com.duckstore.service.packaging.PackagingPlan;
import com.duckstore.service.packaging.PackagingService;
import com.duckstore.service.pricing.PriceBreakdown;
import com.duckstore.service.pricing.PricingInput;
import com.duckstore.service.pricing.PriceCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Facade over three independent concerns: which price applies, how to package, what it costs. */
@Service
public class OrderQuoteService {

    private final PriceResolver priceResolver;
    private final PackagingService packagingService;
    private final PriceCalculator priceCalculator;

    public OrderQuoteService(PriceResolver priceResolver, PackagingService packagingService,
                             PriceCalculator priceCalculator) {
        this.priceResolver = priceResolver;
        this.packagingService = packagingService;
        this.priceCalculator = priceCalculator;
    }

    @Transactional(readOnly = true)
    public OrderQuote quote(OrderCommand order) {
        Duck duck = priceResolver.resolve(order.color(), order.size(), order.quantity());
        PackagingPlan packaging = packagingService.plan(order.size(), order.shippingMode());
        PriceBreakdown price = priceCalculator.price(new PricingInput(duck.getPrice(), order.quantity(),
                packaging.packageType(), Destination.from(order.destinationCountry()), order.shippingMode()));
        return new OrderQuote(duck.getId(), packaging, price);
    }
}
