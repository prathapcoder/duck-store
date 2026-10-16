package com.duckstore.testsupport;

/** Test data builder for the "add duck" request body. Defaults are valid, so a test only states what matters. */
public final class DuckJson {

    private String color = "Red";
    private String size = "Large";
    private String price = "10.00";
    private int quantity = 1;

    private DuckJson() {
    }

    public static DuckJson aDuck() {
        return new DuckJson();
    }

    public DuckJson color(String color) {
        this.color = color;
        return this;
    }

    public DuckJson size(String size) {
        this.size = size;
        return this;
    }

    public DuckJson price(String price) {
        this.price = price;
        return this;
    }

    public DuckJson quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public String build() {
        return "{\"color\":\"%s\",\"size\":\"%s\",\"price\":%s,\"quantity\":%d}".formatted(color, size, price, quantity);
    }
}
