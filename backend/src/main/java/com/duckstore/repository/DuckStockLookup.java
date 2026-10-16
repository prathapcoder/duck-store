package com.duckstore.repository;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import com.duckstore.entity.Duck;
import com.duckstore.service.StockLookup;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Adapter: lets the service layer ask its narrow question without knowing about Spring Data. */
@Component
public class DuckStockLookup implements StockLookup {

    private final DuckRepository ducks;

    public DuckStockLookup(DuckRepository ducks) {
        this.ducks = ducks;
    }

    @Override
    public Optional<Duck> findCheapestInStock(Color color, Size size, int minQuantity) {
        return ducks.findFirstByDeletedFalseAndColorAndSizeAndQuantityGreaterThanEqualOrderByPriceAscIdAsc(
                color, size, Math.max(minQuantity, 1));
    }
}
