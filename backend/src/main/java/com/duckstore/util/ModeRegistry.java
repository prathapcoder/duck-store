package com.duckstore.util;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Holds exactly one strategy per enum value and fails at startup if one is missing or duplicated.
 * Adding a shipping mode therefore forces a packaging strategy and a shipping-charge strategy to exist.
 */
public final class ModeRegistry<K extends Enum<K>, S> {

    private final Map<K, S> byKey;

    public ModeRegistry(Class<K> keyType, Collection<S> strategies, Function<S, K> keyOf, String description) {
        this.byKey = new EnumMap<>(keyType);
        for (S strategy : strategies) {
            K key = keyOf.apply(strategy);
            if (byKey.put(key, strategy) != null) {
                throw new IllegalStateException("More than one " + description + " registered for " + key);
            }
        }
        for (K key : keyType.getEnumConstants()) {
            if (!byKey.containsKey(key)) {
                throw new IllegalStateException("No " + description + " registered for " + key);
            }
        }
    }

    public S get(K key) {
        return byKey.get(key);
    }
}
