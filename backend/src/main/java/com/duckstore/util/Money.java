package com.duckstore.util;

import com.duckstore.exceptions.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** All money is BigDecimal at 2 decimals, rounded HALF_UP, so results are exact to the cent. */
public final class Money {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private Money() {
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentOf(BigDecimal base, int percent) {
        return base.multiply(BigDecimal.valueOf(percent)).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /** Accepts amounts that are already whole cents (12, 12.5, 12.50); rejects 12.505 instead of rounding it. */
    public static BigDecimal toCents(BigDecimal value) {
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new BadRequestException("Prices support at most 2 decimal places");
        }
    }
}
