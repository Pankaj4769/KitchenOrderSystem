package com.kos.validation;

import com.kos.exception.SubscriptionExpiredException;
import com.kos.model.Subscription;
import com.kos.model.Subscription.SubscriptionStatus;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class SubscriptionValidator {

    public void validateActive(Subscription subscription) {
        if (subscription == null) {
            throw new SubscriptionExpiredException("No subscription found");
        }
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new SubscriptionExpiredException("Subscription is cancelled");
        }
        if (subscription.getStatus() == SubscriptionStatus.EXPIRED ||
                subscription.getExpiryDate().isBefore(LocalDate.now())) {
            throw new SubscriptionExpiredException("Subscription expired on "
                    + subscription.getExpiryDate());
        }
    }
}
