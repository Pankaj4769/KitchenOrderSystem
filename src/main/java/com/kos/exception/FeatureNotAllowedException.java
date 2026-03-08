package com.kos.exception;

public class FeatureNotAllowedException extends RuntimeException {
    public FeatureNotAllowedException(String message) {
        super(message);
    }
}
