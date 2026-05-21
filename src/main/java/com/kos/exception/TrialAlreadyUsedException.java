package com.kos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TrialAlreadyUsedException extends RuntimeException {

    public TrialAlreadyUsedException(Long restaurantId) {
        super("Restaurant " + restaurantId + " has already used a free trial or has an active subscription");
    }

    public TrialAlreadyUsedException(String restaurantName, Long restaurantId) {
        super(buildMessage(restaurantName, restaurantId));
    }

    private static String buildMessage(String restaurantName, Long restaurantId) {
        String label = (restaurantName != null && !restaurantName.isBlank())
                ? restaurantName
                : "Restaurant " + restaurantId;
        return label + " has already used a free trial or has an active subscription";
    }
}
