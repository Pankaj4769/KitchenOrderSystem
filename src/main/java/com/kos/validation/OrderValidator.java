package com.kos.validation;

import com.kos.dto.Order;
import com.kos.exception.InvalidInputException;

public class OrderValidator {

    public static void validate(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new InvalidInputException("Order must contain at least one item");
        }
    }
}
