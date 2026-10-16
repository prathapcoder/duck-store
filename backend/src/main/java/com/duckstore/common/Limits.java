package com.duckstore.common;

public final class Limits {

    /** Largest quantity for one duck record or one order. Keeps every sum far from int overflow. */
    public static final int MAX_QUANTITY = 1_000_000_000;

    private Limits() {
    }
}
