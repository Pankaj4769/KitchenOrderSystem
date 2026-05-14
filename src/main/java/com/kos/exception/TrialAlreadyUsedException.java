package com.kos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TrialAlreadyUsedException extends RuntimeException {
    public TrialAlreadyUsedException(Long restaurantId) {
        super("Restaurant " + restaurantId + " has already used a free trial or has an active subscription");
    }
}
