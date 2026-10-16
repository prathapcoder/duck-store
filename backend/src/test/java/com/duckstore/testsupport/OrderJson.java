package com.duckstore.testsupport;

/** Test data builder for the order request body. */
public final class OrderJson {

    private String color = "Red";
    private String size = "XLarge";
    private int quantity = 10;
    private String country = "USA";
    private String mode = "Air";

    private OrderJson() {
    }

    public static OrderJson anOrder() {
        return new OrderJson();
    }

    public OrderJson color(String color) {
        this.color = color;
        return this;
    }

    public OrderJson size(String size) {
        this.size = size;
        return this;
    }

    public OrderJson quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderJson country(String country) {
        this.country = country;
        return this;
    }

    public OrderJson mode(String mode) {
        this.mode = mode;
        return this;
    }

    public String build() {
        return ("{\"color\":\"%s\",\"size\":\"%s\",\"quantity\":%d,\"destinationCountry\":\"%s\",\"shippingMode\":\"%s\"}")
                .formatted(color, size, quantity, country, mode);
    }
}
