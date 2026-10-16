package com.duckstore.common;

import java.util.Arrays;
import java.util.stream.Collectors;

/** An enum whose public name is a display label such as "XLarge". Parsing is case-insensitive. */
public interface Labelled {

    String label();

    static <E extends Enum<E> & Labelled> E parse(Class<E> type, String value) {
        if (value == null) {
            return null;
        }
        String wanted = value.trim();
        for (E constant : type.getEnumConstants()) {
            if (constant.label().equalsIgnoreCase(wanted) || constant.name().equalsIgnoreCase(wanted)) {
                return constant;
            }
        }
        String allowed = Arrays.stream(type.getEnumConstants()).map(Labelled::label).collect(Collectors.joining(", "));
        throw new IllegalArgumentException(
                "Unknown " + type.getSimpleName().toLowerCase() + " '" + value + "'. Allowed values: " + allowed);
    }
}
