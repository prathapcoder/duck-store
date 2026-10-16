package com.duckstore.service.pricing;

import java.util.List;

/** One pricing concern. Rules run in @Order; each sees what earlier rules did through the context. */
public interface PricingRule {

    List<Adjustment> evaluate(PricingContext context);
}
