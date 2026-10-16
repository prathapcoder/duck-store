package com.duckstore.common;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PackageType {
    WOOD("Wood"), CARDBOARD("Cardboard"), PLASTIC("Plastic");

    private final String label;

    PackageType(String label) {
        this.label = label;
    }

    @JsonValue
    public String label() {
        return label;
    }
}
