package com.duckstore.service;

import com.duckstore.common.Color;
import com.duckstore.common.Limits;
import com.duckstore.common.Size;
import com.duckstore.entity.Duck;
import com.duckstore.exceptions.ConflictException;
import com.duckstore.repository.DuckRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Single responsibility: add ducks so that at most one active record exists per color, size and price,
 * safely under concurrent requests. The merge is an atomic database increment; the unique key on active
 * ducks makes two simultaneous creations impossible, and the loser of that race retries and merges.
 */
@Component
public class DuckMerger {

    private static final int MAX_ATTEMPTS = 5;

    private final DuckRepository ducks;
    private final TransactionTemplate transactions;

    public DuckMerger(DuckRepository ducks, TransactionTemplate transactions) {
        this.ducks = ducks;
        this.transactions = transactions;
    }

    public SaveResult mergeOrCreate(Color color, Size size, BigDecimal price, int quantity) {
        String key = Duck.activeKeyOf(color, size, price);
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                Optional<SaveResult> saved = transactions.execute(status -> tryOnce(key, color, size, price, quantity));
                if (saved.isPresent()) {
                    return saved.get();
                }
            } catch (DataIntegrityViolationException | ConcurrencyFailureException lostRace) {
                // another request created or removed the same duck between our steps; try again
            }
        }
        throw new ConflictException("Could not add the duck because of concurrent changes. Please retry.");
    }
    private Optional<SaveResult> tryOnce(String key, Color color, Size size, BigDecimal price, int quantity) {
        int room = Limits.MAX_QUANTITY - quantity;
        if (ducks.addQuantity(key, quantity, room) > 0) {
            return ducks.findByActiveKey(key).map(duck -> new SaveResult(duck, false));
        }
        Optional<Duck> existing = ducks.findByActiveKey(key);
        if (existing.isEmpty()) {
            return Optional.of(new SaveResult(ducks.saveAndFlush(new Duck(color, size, price, quantity)), true));
        }
        if (existing.get().getQuantity() > room) {
            throw new ConflictException("Adding " + quantity + " would exceed the maximum stock of "
                    + Limits.MAX_QUANTITY + " for this duck.");
        }
        // A concurrent request created this duck after our update found nothing: retry, and the update will merge into it.
        return Optional.empty();
    }
}
