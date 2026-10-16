package com.duckstore.exceptions;

import org.springframework.http.HttpStatus;

/** Base of every error the API reports on purpose. Each subclass says which HTTP status it means. */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
