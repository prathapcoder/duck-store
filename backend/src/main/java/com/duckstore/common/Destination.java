package com.duckstore.common;

import com.duckstore.util.Texts;
import java.util.Set;

/**
 * Destinations that have their own surcharge. Anything else is {@link #OTHER}.
 * Input may be a name or an ISO code ("USA", "us", "United States", "Bolivia", "BO", "India"...).
 */
public enum Destination {
    USA("USA", Set.of("usa", "us", "united states", "united states of america")),
    BOLIVIA("Bolivia", Set.of("bolivia", "bo", "bol", "plurinational state of bolivia")),
    INDIA("India", Set.of("india", "in", "ind", "bharat")),
    OTHER("other destination", Set.of());

    private final String label;
    private final Set<String> aliases;

    Destination(String label, Set<String> aliases) {
        this.label = label;
        this.aliases = aliases;
    }

    public String label() {
        return label;
    }

    public static Destination from(String country) {
        String key = Texts.normalize(country);
        for (Destination destination : values()) {
            if (destination.aliases.contains(key)) {
                return destination;
            }
        }
        return OTHER;
    }
}
