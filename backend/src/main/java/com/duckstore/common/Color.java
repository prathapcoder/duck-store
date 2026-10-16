package com.duckstore.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Color implements Labelled {
    RED("Red"), GREEN("Green"), YELLOW("Yellow"), BLACK("Black");

    private final String label;

    Color(String label) {
        this.label = label;
    }

    @Override
    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator
    public static Color fromValue(String value) {
        return Labelled.parse(Color.class, value);
    }
}
