package com.duckstore.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Size implements Labelled {
    XLARGE("XLarge", 5), LARGE("Large", 4), MEDIUM("Medium", 3), SMALL("Small", 2), XSMALL("XSmall", 1);

    private final String label;
    private final int rank;

    Size(String label, int rank) {
        this.label = label;
        this.rank = rank;
    }

    @Override
    @JsonValue
    public String label() {
        return label;
    }

    /** Physical magnitude, 1 (XSmall) to 5 (XLarge). Used to sort sizes in a meaningful order. */
    public int rank() {
        return rank;
    }

    @JsonCreator
    public static Size fromValue(String value) {
        return Labelled.parse(Size.class, value);
    }
}
