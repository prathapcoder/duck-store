package com.duckstore.service;

import com.duckstore.entity.Duck;

/** Outcome of "add duck": the stored duck, and whether a new record was created (false means it was merged). */
public record SaveResult(Duck duck, boolean created) {
}
