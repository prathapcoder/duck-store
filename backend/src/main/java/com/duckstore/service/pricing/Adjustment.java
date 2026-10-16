package com.duckstore.service.pricing;

import com.duckstore.util.Money;
import java.math.BigDecimal;

/** One line of the breakdown. {@code amount} is signed: negative for a discount, positive for an increment. */
public record Adjustment(String code, String description, BigDecimal amount) {

    public Adjustment {
        amount = Money.round(amount);
    }

    public static Adjustment increment(String code, String description, BigDecimal amount) {
        return new Adjustment(code, description, amount);
    }

    public static Adjustment discount(String code, String description, BigDecimal amount) {
        return new Adjustment(code, description, amount.negate());
    }
}
