package com.duckstore.service;

import com.duckstore.exceptions.BadRequestException;

public enum Direction {
    ASC, DESC;

    public static Direction parse(String value) {
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Unknown sort direction '" + value + "'. Allowed values: asc, desc");
        }
    }
}
