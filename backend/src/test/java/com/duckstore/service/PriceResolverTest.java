package com.duckstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import com.duckstore.entity.Duck;
import com.duckstore.exceptions.ConflictException;
import com.duckstore.exceptions.NotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** PriceResolver depends only on the narrow StockLookup port, so a lambda is all the test needs. */
class PriceResolverTest {

    private final Duck cheapLot = new Duck(Color.RED, Size.LARGE, new BigDecimal("9.00"), 5);

    /** A warehouse with one lot of 5 ducks. */
    private final StockLookup oneLotOfFive =
            (color, size, minQuantity) -> minQuantity <= 5 ? Optional.of(cheapLot) : Optional.empty();

    private final StockLookup empty = (color, size, minQuantity) -> Optional.empty();

    @Test
    void uses_the_lot_returned_as_cheapest_in_stock_even_for_a_bigger_order() {
        assertThat(new PriceResolver(oneLotOfFive, false).resolve(Color.RED, Size.LARGE, 500)).isSameAs(cheapLot);
    }

    @Test
    void with_enforce_stock_the_whole_order_must_fit_in_one_lot() {
        PriceResolver resolver = new PriceResolver(oneLotOfFive, true);

        assertThat(resolver.resolve(Color.RED, Size.LARGE, 5)).isSameAs(cheapLot);
        assertThatThrownBy(() -> resolver.resolve(Color.RED, Size.LARGE, 6)).isInstanceOf(ConflictException.class);
    }

    @Test
    void nothing_in_stock_is_not_found() {
        assertThatThrownBy(() -> new PriceResolver(empty, false).resolve(Color.RED, Size.LARGE, 1))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> new PriceResolver(empty, true).resolve(Color.RED, Size.LARGE, 1))
                .isInstanceOf(NotFoundException.class);
    }
}
