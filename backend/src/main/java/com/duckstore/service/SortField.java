package com.duckstore.service;

import com.duckstore.entity.Duck;
import com.duckstore.exceptions.BadRequestException;
import java.util.Comparator;

/** The sortable columns and how each one compares. Sizes compare by physical magnitude, not by name. */
public enum SortField {
    ID(Comparator.comparing(Duck::getId)),
    COLOR(Comparator.comparing(duck -> duck.getColor().label())),
    SIZE(Comparator.comparingInt(duck -> duck.getSize().rank())),
    PRICE(Comparator.comparing(Duck::getPrice)),
    QUANTITY(Comparator.comparingInt(Duck::getQuantity));

    private final Comparator<Duck> comparator;

    SortField(Comparator<Duck> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Duck> comparator() {
        return comparator;
    }

    public static SortField parse(String value) {
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Unknown sort field '" + value + "'. Allowed values: id, color, size, price, quantity");
        }
    }
}
