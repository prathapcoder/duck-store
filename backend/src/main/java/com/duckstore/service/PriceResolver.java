package com.duckstore.service;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import com.duckstore.entity.Duck;
import com.duckstore.exceptions.ConflictException;
import com.duckstore.exceptions.NotFoundException;

/**
 * Decides which warehouse record supplies the unit price when several prices exist for a color and
 * size: the cheapest lot that is in stock (and, with enforce-stock on, large enough for the whole
 * order). Lots are never mixed within one order.
 */
public class PriceResolver {

    private final StockLookup stock;
    private final boolean enforceStock;

    public PriceResolver(StockLookup stock, boolean enforceStock) {
        this.stock = stock;
        this.enforceStock = enforceStock;
    }

    public Duck resolve(Color color, Size size, int quantity) {
        int needed = enforceStock ? quantity : 1;
        return stock.findCheapestInStock(color, size, needed).orElseThrow(() -> unavailable(color, size, quantity));
    }

    private RuntimeException unavailable(Color color, Size size, int quantity) {
        boolean someStock = enforceStock && stock.findCheapestInStock(color, size, 1).isPresent();
        if (someStock) {
            return new ConflictException("Not enough stock: no single price lot of " + color.label() + " "
                    + size.label() + " ducks has " + quantity + " units available.");
        }
        return new NotFoundException("No " + color.label() + " " + size.label() + " ducks are in stock.");
    }
}
