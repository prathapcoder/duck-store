package com.duckstore.common;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Protection {
    POLYSTYRENE_BALLS("Polystyrene balls"),
    BUBBLE_WRAP_BAGS("Bubble-wrap bags"),
    MOISTURE_ABSORBING_BEADS("Moisture-absorbing beads");

    private final String label;

    Protection(String label) {
        this.label = label;
    }

    @JsonValue
    public String label() {
        return label;
    }
}
