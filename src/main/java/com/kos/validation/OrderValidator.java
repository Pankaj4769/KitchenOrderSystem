package com.kos.validation;

import com.kos.dto.Order;

public class OrderValidator {

    public static void validate(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new InvalidInputException("Order must contain at least one item");
        }
    }
}
