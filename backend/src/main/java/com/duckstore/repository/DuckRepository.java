package com.duckstore.repository;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import com.duckstore.entity.Duck;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Portable on purpose: derived queries and one JPQL update, no database-specific SQL. */
public interface DuckRepository extends JpaRepository<Duck, Integer> {

    List<Duck> findByDeletedFalse();

    Optional<Duck> findByIdAndDeletedFalse(Integer id);

    Optional<Duck> findByActiveKey(String activeKey);

    boolean existsByActiveKey(String activeKey);

    Optional<Duck> findFirstByDeletedFalseAndColorAndSizeAndQuantityGreaterThanEqualOrderByPriceAscIdAsc(
            Color color, Size size, int minQuantity);

    /**
     * Atomic increment done by the database in one statement, so concurrent adds cannot lose updates.
     * {@code room} is the headroom below the maximum quantity; with no headroom nothing is updated.
     * Returns the number of rows changed (0 when there is no active duck with that key, or no room).
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Duck d set d.quantity = d.quantity + :amount where d.activeKey = :activeKey and d.quantity <= :room")
    int addQuantity(@Param("activeKey") String activeKey, @Param("amount") int amount, @Param("room") int room);
}
