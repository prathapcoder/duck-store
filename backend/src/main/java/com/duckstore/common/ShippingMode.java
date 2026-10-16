package com.duckstore.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ShippingMode implements Labelled {
    LAND("Land"), AIR("Air"), SEA("Sea");

    private final String label;

    ShippingMode(String label) {
        this.label = label;
    }

    @Override
    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator
    public static ShippingMode fromValue(String value) {
        return Labelled.parse(ShippingMode.class, value);
    }
}
