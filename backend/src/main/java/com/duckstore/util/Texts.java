package com.duckstore.util;

import java.util.Locale;

public final class Texts {

    private Texts() {
    }

    /** Lower-case, trimmed, dots removed, inner whitespace collapsed: " U.S.A. " becomes "usa". */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace(".", "").replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
