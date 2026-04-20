package com.kos.validation;

import java.util.Arrays;
import java.util.List;

import com.kos.exception.InvalidInputException;

public class OrderStatusValidator {

    private static final List<String> VALID_STATUSES =
            Arrays.asList("PENDING", "PREPARING", "READY", "SERVED", "CANCELLED");

    public static void validate(String status) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new InvalidInputException(
                "Invalid order status. Allowed values: PENDING, PREPARING, READY, SERVED, CANCELLED"
            );
        }
    }
}
