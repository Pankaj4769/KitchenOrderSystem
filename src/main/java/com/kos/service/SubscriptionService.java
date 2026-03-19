package com.kos.service;

import com.kos.dto.SubscriptionRequestDTO;
import com.kos.dto.SubscriptionResponseDTO;
import com.kos.model.SubscriptionPlan.PlanType;
import java.util.List;

public interface SubscriptionService {

    SubscriptionResponseDTO assignPlan(SubscriptionRequestDTO request);

    SubscriptionResponseDTO upgradePlan(Long restaurantId, PlanType newPlan);

    SubscriptionResponseDTO getSubscription(Long restaurantId);

    List<SubscriptionResponseDTO> getAllPlans();

    void cancelSubscription(Long restaurantId);

    boolean hasFeatureAccess(Long restaurantId, String feature);

    void checkAndExpireSubscriptions();  // scheduled job
}
