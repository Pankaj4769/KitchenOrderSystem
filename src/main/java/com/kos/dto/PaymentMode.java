package com.kos.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMode {

    FULL("full"),
    SPLIT("split"),
    PART("part");

    private final String value;

    PaymentMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PaymentMode fromValue(String value) {
        for (PaymentMode mode : PaymentMode.values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentMode: " + value);
    }
}