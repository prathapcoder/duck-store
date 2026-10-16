package com.duckstore.service.pricing;

import com.duckstore.common.ShippingMode;
import java.util.List;

/** Strategy: the shipping charge lines for one shipping mode. A new mode is one new implementation. */
public interface ShippingChargeStrategy {

    ShippingMode mode();

    List<Adjustment> charges(int quantity);
}
