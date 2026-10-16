package com.duckstore.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import com.duckstore.repository.DuckRepository;
import com.duckstore.service.DuckChange;
import com.duckstore.service.DuckService;
import com.duckstore.service.event.WarehouseEvent;
import com.duckstore.service.event.WarehouseListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/** Proves the use cases publish the right events, and only after the transaction commits. */
@SpringBootTest
@Import(WarehouseEventsTest.Recording.class)
class WarehouseEventsTest {

    static final List<WarehouseEvent> HEARD = new CopyOnWriteArrayList<>();

    @TestConfiguration
    static class Recording {
        @Bean
        WarehouseListener recordingListener() {
            return HEARD::add;
        }
    }

    @Autowired
    private DuckService service;

    @Autowired
    private DuckRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
        HEARD.clear();
    }

    @Test
    void add_merge_update_and_delete_each_publish_one_event() {
        int id = service.add(Color.RED, Size.LARGE, new BigDecimal("10.00"), 5).duck().getId();
        service.add(Color.RED, Size.LARGE, new BigDecimal("10.00"), 5);
        service.update(id, new DuckChange(null, null, new BigDecimal("11.00"), 2));
        service.delete(id);

        assertThat(HEARD).extracting(WarehouseEvent::kind).containsExactly(
                WarehouseEvent.Kind.CREATED, WarehouseEvent.Kind.MERGED,
                WarehouseEvent.Kind.UPDATED, WarehouseEvent.Kind.DELETED);
        assertThat(HEARD.get(1).quantity()).isEqualTo(10);
    }

    @Test
    void a_rejected_change_publishes_nothing() {
        service.add(Color.RED, Size.LARGE, new BigDecimal("10.00"), 5);
        service.add(Color.RED, Size.LARGE, new BigDecimal("11.00"), 5);
        HEARD.clear();
        int id = repository.findAll().stream().filter(d -> d.getPrice().compareTo(new BigDecimal("10.00")) == 0)
                .findFirst().orElseThrow().getId();

        try {
            service.update(id, new DuckChange(null, null, new BigDecimal("11.00"), 1));
        } catch (RuntimeException expected) {
            // the 409 conflict
        }

        assertThat(HEARD).isEmpty();
    }
}
