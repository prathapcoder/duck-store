package com.duckstore.service;

import com.duckstore.common.Color;
import com.duckstore.common.Limits;
import com.duckstore.common.Size;
import com.duckstore.entity.Duck;
import com.duckstore.exceptions.BadRequestException;
import com.duckstore.exceptions.ConflictException;
import com.duckstore.exceptions.NotFoundException;
import com.duckstore.repository.DuckRepository;
import com.duckstore.service.event.WarehouseEvent;
import com.duckstore.service.event.WarehouseEventPublisher;
import com.duckstore.util.Money;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The warehouse use cases. The concurrency-sensitive "add" is delegated to {@link DuckMerger}. */
@Service
public class DuckService {

    private final DuckRepository ducks;
    private final DuckMerger merger;
    private final WarehouseEventPublisher events;

    public DuckService(DuckRepository ducks, DuckMerger merger, WarehouseEventPublisher events) {
        this.ducks = ducks;
        this.merger = merger;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<Duck> list(SortField field, Direction direction) {
        Comparator<Duck> order = direction == Direction.DESC ? field.comparator().reversed() : field.comparator();
        return ducks.findByDeletedFalse().stream().sorted(order.thenComparing(Duck::getId)).toList();
    }

    @Transactional(readOnly = true)
    public Duck get(int id) {
        return ducks.findByIdAndDeletedFalse(id).orElseThrow(() -> notFound(id));
    }

    public SaveResult add(Color color, Size size, BigDecimal price, int quantity) {
        requireQuantity(1, quantity);
        SaveResult result = merger.mergeOrCreate(color, size, Money.toCents(price), quantity);
        events.publish(WarehouseEvent.of(result.created() ? WarehouseEvent.Kind.CREATED : WarehouseEvent.Kind.MERGED, result.duck()));
        return result;
    }

    @Transactional
    public Duck update(int id, DuckChange change) {
        requireQuantity(0, change.quantity());
        Duck duck = get(id);
        rejectIfChanged("color", change.color(), duck.getColor());
        rejectIfChanged("size", change.size(), duck.getSize());
        BigDecimal cents = Money.toCents(change.price());
        duck.change(cents, change.quantity());
        Duck saved;
        try {
            saved = ducks.saveAndFlush(duck);
        } catch (DataIntegrityViolationException duplicate) {
            throw new ConflictException("Another duck with the same color, size and price " + cents
                    + " already exists. Edit that record's quantity instead.");
        }
        events.publish(WarehouseEvent.of(WarehouseEvent.Kind.UPDATED, saved));
        return saved;
    }

    @Transactional
    public void delete(int id) {
        Duck duck = get(id);
        duck.markDeleted();
        events.publish(WarehouseEvent.of(WarehouseEvent.Kind.DELETED, duck));
    }

    private static void requireQuantity(int minimum, int quantity) {
        if (quantity < minimum || quantity > Limits.MAX_QUANTITY) {
            throw new BadRequestException("quantity must be between " + minimum + " and " + Limits.MAX_QUANTITY);
        }
    }

    private static <T> void rejectIfChanged(String field, T requested, T current) {
        if (requested != null && !requested.equals(current)) {
            throw new BadRequestException(field + " cannot be changed; only price and quantity can be edited");
        }
    }

    private static NotFoundException notFound(int id) {
        return new NotFoundException("Duck " + id + " not found");
    }
}
