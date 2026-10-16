package com.duckstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.duckstore.common.ShippingMode;
import com.duckstore.util.ModeRegistry;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class ModeRegistryTest {

    private static final Function<String, ShippingMode> BY_NAME = name -> ShippingMode.fromValue(name);

    @Test
    void returns_the_strategy_registered_for_each_key() {
        ModeRegistry<ShippingMode, String> registry =
                new ModeRegistry<>(ShippingMode.class, List.of("land", "air", "sea"), BY_NAME, "thing");

        assertThat(registry.get(ShippingMode.AIR)).isEqualTo("air");
    }

    @Test
    void a_missing_key_fails_at_construction() {
        assertThatThrownBy(() -> new ModeRegistry<>(ShippingMode.class, List.of("land", "air"), BY_NAME, "thing"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("SEA");
    }

    @Test
    void a_duplicate_key_fails_at_construction() {
        assertThatThrownBy(() -> new ModeRegistry<>(ShippingMode.class, List.of("land", "air", "sea", "Sea"), BY_NAME, "thing"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("More than one");
    }
}
