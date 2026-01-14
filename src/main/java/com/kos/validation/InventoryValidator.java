package com.kos.validation;

import com.kos.dto.Item;


public class InventoryValidator {

    public static void validateQuantity(Item item) {
        if (item.getItemQuantity() == null || item.getItemQuantity() == 0) {
            throw new InvalidInputException("Item quantity cannot be zero or empty");
        }
        if (item.getItemName() == null || item.getItemName().equals("")) {
            throw new InvalidInputException("Item Name cannot be empty");
        }
        if (item.getItemPrice() == null || item.getItemPrice() <= 0) {
            throw new InvalidInputException("Item Price cannot be zero or empty");
        }
    }

    public static void validateItemId(Item item) {
        if (item.getItemId() == null) {
            throw new InvalidInputException("Item ID is required");
        }
    }
}
