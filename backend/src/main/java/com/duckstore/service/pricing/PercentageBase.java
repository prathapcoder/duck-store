package com.duckstore.service.pricing;

/** What "x% of the total cost" is measured against. See README, "Decisions". */
public enum PercentageBase {
    /** The total so far, after the adjustments applied before this one (default). */
    RUNNING_TOTAL,
    /** Always quantity x unit price, so every percentage is independent of the others. */
    SUBTOTAL
}
