package com.duckstore.config;

import com.duckstore.service.PriceResolver;
import com.duckstore.service.StockLookup;
import com.duckstore.service.pricing.PercentageBase;
import com.duckstore.service.pricing.PricingRule;
import com.duckstore.service.pricing.BreakdownVerifier;
import com.duckstore.service.pricing.PriceCalculator;
import com.duckstore.service.pricing.PricingService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the configurable services, keeping Spring out of the classes themselves. The price calculator is decorated with a verifier. */
@Configuration
public class StoreConfig {

    @Bean
    PriceCalculator priceCalculator(List<PricingRule> rules,
            @Value("${duckstore.pricing.percentage-base:RUNNING_TOTAL}") PercentageBase percentageBase) {
        return new BreakdownVerifier(new PricingService(rules, percentageBase));
    }

    @Bean
    PriceResolver priceResolver(StockLookup stock,
            @Value("${duckstore.store.enforce-stock:false}") boolean enforceStock) {
        return new PriceResolver(stock, enforceStock);
    }
}
