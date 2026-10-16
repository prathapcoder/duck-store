package com.duckstore.service;

import com.duckstore.common.Color;
import com.duckstore.common.ShippingMode;
import com.duckstore.common.Size;

/** What the service layer needs to price an order; the HTTP request is translated into this. */
public record OrderCommand(Color color, Size size, int quantity, String destinationCountry, ShippingMode shippingMode) {
}
