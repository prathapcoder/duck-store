package com.duckstore.service;

import com.duckstore.service.packaging.PackagingPlan;
import com.duckstore.service.pricing.PriceBreakdown;

/** The result of pricing an order, independent of how it is presented over HTTP. */
public record OrderQuote(int duckId, PackagingPlan packaging, PriceBreakdown price) {
}
