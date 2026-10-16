package com.duckstore.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import com.duckstore.service.event.WarehouseEvent;
import com.duckstore.service.event.WarehouseEventPublisher;
import com.duckstore.service.event.WarehouseListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WarehouseEventPublisherTest {

    private static final WarehouseEvent EVENT = new WarehouseEvent(WarehouseEvent.Kind.CREATED, 1, Color.RED,
            Size.LARGE, new BigDecimal("10.00"), 5);

    @Test
    void every_listener_is_notified() {
        List<WarehouseEvent> first = new ArrayList<>();
        List<WarehouseEvent> second = new ArrayList<>();
        new WarehouseEventPublisher(List.of(first::add, second::add)).publish(EVENT);

        assertThat(first).containsExactly(EVENT);
        assertThat(second).containsExactly(EVENT);
    }

    @Test
    void a_failing_listener_does_not_stop_the_others_or_the_caller() {
        List<WarehouseEvent> heard = new ArrayList<>();
        WarehouseListener failing = event -> {
            throw new IllegalStateException("boom");
        };

        new WarehouseEventPublisher(List.of(failing, heard::add)).publish(EVENT);

        assertThat(heard).containsExactly(EVENT);
    }
}
