package com.amalitech.dovewing;

/**
 * Thrown when the depot refuses a rent or return because of the situation at the counter
 * (car already out, unknown plate, bad number of days). It is checked on purpose: these are
 * expected, recoverable events, so the caller must decide what to do. Programming mistakes
 * (null arguments, invalid objects) stay unchecked.
 */
public final class RentalRefusedException extends Exception {
    public RentalRefusedException(String message) {
        super(message);
    }
}