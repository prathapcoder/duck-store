package com.duckstore.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import com.duckstore.entity.Duck;
import com.duckstore.repository.DuckRepository;
import com.duckstore.service.DuckService;
import com.duckstore.service.SaveResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Many simultaneous adds of the same duck must give one record and the exact total quantity. */
@SpringBootTest
class DuckConcurrencyTest {

    @Autowired
    private DuckService service;

    @Autowired
    private DuckRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void concurrent_adds_never_create_duplicates_or_lose_quantity() throws Exception {
        int threads = 32;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<SaveResult>> results = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            results.add(pool.submit(() -> {
                start.await();
                return service.add(Color.GREEN, Size.SMALL, new BigDecimal("7.25"), 5);
            }));
        }
        start.countDown();
        int created = 0;
        for (Future<SaveResult> result : results) {
            if (result.get(60, TimeUnit.SECONDS).created()) {
                created++;
            }
        }
        pool.shutdown();

        List<Duck> ducks = repository.findByDeletedFalse();
        assertThat(created).isEqualTo(1);
        assertThat(ducks).hasSize(1);
        assertThat(ducks.get(0).getQuantity()).isEqualTo(threads * 5);
    }
}
