package com.kos.repository;

import com.kos.model.Subscription;
import com.kos.model.Subscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByRestaurantIdAndStatus(Long restaurantId, SubscriptionStatus status);

    List<Subscription> findByExpiryDateBeforeAndStatus(LocalDate date, SubscriptionStatus status);

    List<Subscription> findByRestaurantId(Long restaurantId);
}
