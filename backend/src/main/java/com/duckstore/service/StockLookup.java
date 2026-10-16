package com.duckstore.service;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import com.duckstore.entity.Duck;
import java.util.Optional;

/**
 * The one thing pricing needs from the warehouse. Owned by the service layer (Dependency Inversion),
 * narrow on purpose (Interface Segregation), and easy to fake with a lambda in a test.
 */
@FunctionalInterface
public interface StockLookup {

    /** The cheapest active lot of this color and size holding at least {@code minQuantity} units. */
    Optional<Duck> findCheapestInStock(Color color, Size size, int minQuantity);
}
