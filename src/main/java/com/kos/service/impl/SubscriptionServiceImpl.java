package com.kos.service.impl;

import com.kos.dto.SubscriptionRequestDTO;
import com.kos.dto.SubscriptionResponseDTO;
import com.kos.exception.FeatureNotAllowedException;
import com.kos.exception.SubscriptionExpiredException;
import com.kos.model.Subscription;
import com.kos.model.Subscription.SubscriptionStatus;
import com.kos.model.SubscriptionPlan;
import com.kos.model.SubscriptionPlan.PlanType;
import com.kos.repository.SubscriptionPlanRepository;
import com.kos.repository.SubscriptionRepository;
import com.kos.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

	private final SubscriptionRepository subscriptionRepository;
	private final SubscriptionPlanRepository planRepository;

	@Autowired
	public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
	                                SubscriptionPlanRepository planRepository) {
	    this.subscriptionRepository = subscriptionRepository;
	    this.planRepository = planRepository;
	}

    @Override
    public SubscriptionResponseDTO assignPlan(SubscriptionRequestDTO request) {
        SubscriptionPlan plan = planRepository.findByPlanName(request.getPlanName())
                .orElseThrow(() -> new RuntimeException("Plan not found: " + request.getPlanName()));

        Subscription subscription = new Subscription();
        subscription.setRestaurantId(request.getRestaurantId());
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDate.now());
        subscription.setExpiryDate(LocalDate.now().plusDays(
                request.getDurationDays() > 0 ? request.getDurationDays() : plan.getDurationDays()
        ));
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        return mapToDTO(subscriptionRepository.save(subscription));
    }

    @Override
    public SubscriptionResponseDTO upgradePlan(Long restaurantId, PlanType newPlan) {
        Subscription existing = subscriptionRepository
                .findByRestaurantIdAndStatus(restaurantId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active subscription found"));

        SubscriptionPlan plan = planRepository.findByPlanName(newPlan)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + newPlan));

        existing.setPlan(plan);
        existing.setStartDate(LocalDate.now());
        existing.setExpiryDate(LocalDate.now().plusDays(plan.getDurationDays()));

        return mapToDTO(subscriptionRepository.save(existing));
    }

    @Override
    public SubscriptionResponseDTO getSubscription(Long restaurantId) {
        Subscription subscription = subscriptionRepository
                .findByRestaurantIdAndStatus(restaurantId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionExpiredException("No active subscription for restaurant: " + restaurantId));
        return mapToDTO(subscription);
    }

    @Override
    public List<SubscriptionResponseDTO> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(this::mapPlanToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelSubscription(Long restaurantId) {
        Subscription subscription = subscriptionRepository
                .findByRestaurantIdAndStatus(restaurantId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active subscription found"));
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);
    }

    @Override
    public boolean hasFeatureAccess(Long restaurantId, String feature) {
        Subscription subscription = subscriptionRepository
                .findByRestaurantIdAndStatus(restaurantId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionExpiredException("No active subscription"));

        if (subscription.getExpiryDate().isBefore(LocalDate.now())) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            throw new SubscriptionExpiredException("Subscription expired on " + subscription.getExpiryDate());
        }

        boolean hasAccess = subscription.getPlan().getAllowedFeatures().contains(feature);
        if (!hasAccess) {
            throw new FeatureNotAllowedException("Feature '" + feature + "' not available in " 
                    + subscription.getPlan().getPlanName() + " plan. Please upgrade.");
        }
        return true;
    }

    @Scheduled(cron = "0 0 0 * * *")  // runs every midnight
    @Override
    public void checkAndExpireSubscriptions() {
        List<Subscription> expired = subscriptionRepository
                .findByExpiryDateBeforeAndStatus(LocalDate.now(), SubscriptionStatus.ACTIVE);
        expired.forEach(s -> s.setStatus(SubscriptionStatus.EXPIRED));
        subscriptionRepository.saveAll(expired);
    }

    // ---- Mappers ----
    private SubscriptionResponseDTO mapToDTO(Subscription s) {
        SubscriptionResponseDTO dto = new SubscriptionResponseDTO();
        dto.setSubscriptionId(s.getId());
        dto.setRestaurantId(s.getRestaurantId());
        dto.setPlanName(s.getPlan().getPlanName());
        dto.setPrice(s.getPlan().getPrice());
        dto.setStartDate(s.getStartDate());
        dto.setExpiryDate(s.getExpiryDate());
        dto.setStatus(s.getStatus());
        dto.setAllowedFeatures(s.getPlan().getAllowedFeatures());
        dto.setDaysRemaining(ChronoUnit.DAYS.between(LocalDate.now(), s.getExpiryDate()));
        return dto;
    }

    private SubscriptionResponseDTO mapPlanToDTO(SubscriptionPlan plan) {
        SubscriptionResponseDTO dto = new SubscriptionResponseDTO();
        dto.setPlanName(plan.getPlanName());
        dto.setPrice(plan.getPrice());
        dto.setAllowedFeatures(plan.getAllowedFeatures());
        return dto;
    }
}
