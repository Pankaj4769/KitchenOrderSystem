package com.kos.service;

import com.kos.dto.AuthUser;
import com.kos.dto.CompleteSetup;
import com.kos.dto.MessageResponse;
import com.kos.dto.OnboardingStatus;
import com.kos.dto.StaffSetup;
import com.kos.dto.StartTrialRequestDTO;
import com.kos.dto.SubscriptionRequestDTO;
import com.kos.dto.SubscriptionResponseDTO;
import com.kos.dto.UpgradePlan;
import com.kos.dto.UserRole;
import com.kos.exception.FeatureNotAllowedException;
import com.kos.exception.SubscriptionExpiredException;
import com.kos.exception.TrialAlreadyUsedException;
import com.kos.model.Subscription;
import com.kos.model.Subscription.SubscriptionStatus;
import com.kos.model.SubscriptionPlan;
import com.kos.model.SubscriptionPlan.PlanType;
import com.kos.payment.ChargeResult;
import com.kos.payment.MandateResult;
import com.kos.payment.PaymentGatewayPort;
import com.kos.repository.SubscriptionPlanRepository;
import com.kos.repository.SubscriptionRepository;
import com.kos.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubscriptionService{

	@Autowired
	SubscriptionRepository subscriptionRepository;
	@Autowired
	SubscriptionPlanRepository planRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserService userService;

	// Change 1: Inject PaymentGatewayPort and trial duration config
	@Autowired
	private PaymentGatewayPort paymentGateway;

	@Value("${subscription.trial.duration-days:14}")
	private int trialDurationDays;

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

    // Change 3: Fix upgradePlan(Long, PlanType) to include TRIAL + swap mandate when TRIAL
    public SubscriptionResponseDTO upgradePlan(Long restaurantId, PlanType newPlanType) {
        Subscription existing = subscriptionRepository
                .findByRestaurantIdAndStatusIn(restaurantId, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL))
                .orElseThrow(() -> new RuntimeException("No active or trial subscription found"));

        SubscriptionPlan newPlan = planRepository.findByPlanName(newPlanType)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + newPlanType));

        // When upgrading from TRIAL, cancel old mandate and create a new one
        if (existing.getStatus() == SubscriptionStatus.TRIAL && existing.getMandateId() != null) {
            paymentGateway.cancelMandate(existing.getMandateId());
            MandateResult newMandate = paymentGateway.createMandate(
                existing.getRestaurantId(), newPlan.getPlanName().name(), newPlan.getPrice());
            existing.setMandateId(newMandate.mandateId());
        }

        existing.setPlan(newPlan);
        existing.setStartDate(LocalDate.now());
        existing.setExpiryDate(LocalDate.now().plusDays(newPlan.getDurationDays()));

        return mapToDTO(subscriptionRepository.save(existing));
    }

    // Change 2: Fix getSubscription to include TRIAL
    public SubscriptionResponseDTO getSubscription(Long restaurantId) {
        Subscription subscription = subscriptionRepository
                .findByRestaurantIdAndStatusIn(restaurantId, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL))
                .orElseThrow(() -> new RuntimeException("No active or trial subscription for restaurant " + restaurantId));
        return mapToDTO(subscription);
    }

    public List<SubscriptionResponseDTO> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(this::mapPlanToDTO)
                .collect(Collectors.toList());
    }

    // Change 4: Fix cancelSubscription to include TRIAL + cancel mandate
    public void cancelSubscription(Long restaurantId) {
        Subscription sub = subscriptionRepository
                .findByRestaurantIdAndStatusIn(restaurantId, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL))
                .orElseThrow(() -> new RuntimeException("No active or trial subscription found"));
        if (sub.getMandateId() != null) {
            paymentGateway.cancelMandate(sub.getMandateId());
        }
        sub.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(sub);
    }

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

    // Change 7: Extended scheduler to also auto-charge expired TRIAL subscriptions
    @Scheduled(cron = "0 0 0 * * *")  // runs every midnight
    public void checkAndExpireSubscriptions() {
        // Existing ACTIVE -> EXPIRED logic
        List<Subscription> expired = subscriptionRepository
                .findByExpiryDateBeforeAndStatus(LocalDate.now(), SubscriptionStatus.ACTIVE);
        for (Subscription s : expired) {
            s.setStatus(SubscriptionStatus.EXPIRED);
            sendExpiryEmail(s);
        }
        subscriptionRepository.saveAll(expired);

        // Auto-charge expired TRIAL subscriptions
        List<Subscription> expiredTrials = subscriptionRepository
                .findByTrialEndDateBeforeAndStatus(LocalDate.now(), SubscriptionStatus.TRIAL);

        for (Subscription trial : expiredTrials) {
            // Re-fetch to prevent double-charge
            Subscription fresh = subscriptionRepository.findById(trial.getId()).orElse(null);
            if (fresh == null || fresh.getStatus() != SubscriptionStatus.TRIAL) continue;

            ChargeResult charge = paymentGateway.chargeMandate(
                fresh.getMandateId(), fresh.getPlan().getPrice());
            if (charge.success()) {
                fresh.setStatus(SubscriptionStatus.ACTIVE);
                fresh.setExpiryDate(LocalDate.now().plusDays(fresh.getPlan().getDurationDays()));
                fresh.setTrialEndDate(null);
            } else {
                fresh.setStatus(SubscriptionStatus.EXPIRED);
                sendExpiryEmail(fresh);
            }
            subscriptionRepository.save(fresh);
        }
    }

    // Change 5: Add startTrial() method
    @Transactional
    public SubscriptionResponseDTO startTrial(StartTrialRequestDTO request) {
        Long restaurantId = request.getRestaurantId();

        // Guard: one trial per restaurant, ever
        boolean hasHistory = subscriptionRepository.existsByRestaurantIdAndStatusNot(
            restaurantId, SubscriptionStatus.CANCELLED);
        if (hasHistory) {
            throw new TrialAlreadyUsedException(restaurantId);
        }

        // Fetch plan
        SubscriptionPlan plan = planRepository
            .findByPlanName(PlanType.valueOf(request.getPlanName()))
            .orElseThrow(() -> new RuntimeException("Unknown plan: " + request.getPlanName()));

        // Capture payment mandate
        MandateResult mandate = paymentGateway.createMandate(
            restaurantId, request.getPlanName(), plan.getPrice());

        // Create trial subscription
        LocalDate today = LocalDate.now();
        LocalDate trialEnd = today.plusDays(trialDurationDays);

        Subscription sub = new Subscription();
        sub.setRestaurantId(restaurantId);
        sub.setPlan(plan);
        sub.setStatus(SubscriptionStatus.TRIAL);
        sub.setStartDate(today);
        sub.setTrialEndDate(trialEnd);
        sub.setExpiryDate(trialEnd);
        sub.setMandateId(mandate.mandateId());

        sub = subscriptionRepository.save(sub);
        return mapToDTO(sub);
    }

    // Change 6: Add completeTrial() method
    @Transactional
    public Map<String, Object> completeTrial(Long restaurantId) {
        Subscription sub = subscriptionRepository
            .findByRestaurantIdAndStatusIn(restaurantId,
                List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL))
            .orElseThrow(() -> new RuntimeException("No active or trial subscription for restaurant " + restaurantId));

        if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
            return Map.of("alreadyConverted", true);
        }

        // Charge the mandate
        ChargeResult charge = paymentGateway.chargeMandate(
            sub.getMandateId(), sub.getPlan().getPrice());

        if (charge.success()) {
            sub.setStatus(SubscriptionStatus.ACTIVE);
            sub.setExpiryDate(LocalDate.now().plusDays(sub.getPlan().getDurationDays()));
            sub.setTrialEndDate(null);
            subscriptionRepository.save(sub);
            return Map.of("alreadyConverted", false, "status", "ACTIVE");
        } else {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);
            sendExpiryEmail(sub);
            return Map.of("alreadyConverted", false, "status", "EXPIRED", "error", charge.message());
        }
    }

    // Change 8: sendExpiryEmail helper (stub — wire actual email service if available)
    private void sendExpiryEmail(Subscription sub) {
        try {
            // Wire to existing email/SMTP service when available
            // e.g., emailService.sendExpiryNotification(sub.getRestaurantId(), sub.getExpiryDate());
        } catch (Exception e) {
            System.err.println("Failed to send expiry email for restaurant " + sub.getRestaurantId() + ": " + e.getMessage());
        }
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
        dto.setTrialActive(s.getStatus() == SubscriptionStatus.TRIAL);
        dto.setTrialEndDate(s.getTrialEndDate());
        return dto;
    }

    private SubscriptionResponseDTO mapPlanToDTO(SubscriptionPlan plan) {
        SubscriptionResponseDTO dto = new SubscriptionResponseDTO();
        dto.setPlanName(plan.getPlanName());
        dto.setPrice(plan.getPrice());
        dto.setAllowedFeatures(plan.getAllowedFeatures());
        return dto;
    }


    @Transactional
    public MessageResponse updateOnboardingStatus(CompleteSetup setup) {

    	MessageResponse resp = new MessageResponse("failure", false);

    	try {
    	for(StaffSetup staff: setup.getRestaurant().getStaff()) {
    		AuthUser user = new AuthUser();
    		user.setEmail(staff.getEmail());
    		user.setMobile(staff.getMobile());
    		user.setName(staff.getName());
    		user.setRole(staff.getRole());
    		user.setFirstTime(false);
    		user.setPassword("password");
    		user.setOnboardingStatus(OnboardingStatus.valueOf(setup.getOnboardingStatus()));
    		user.setRestaurantId(setup.getRestaurentId());
    		user.setSubscriptionPlan(setup.getPlan());
    		// username = mobile number
    		user.setUsername(staff.getMobile());
    		// Generate and set temp password
    		String tempPassword = userService.generateTempPassword();
    		user.setPassword(tempPassword);
    		userRepository.save(user);
    		// Send credentials via email
    		userService.sendStaffCredentials(staff.getEmail(), staff.getName(), staff.getMobile(), tempPassword);
    	}



    	Optional<AuthUser> dbUser = userRepository.findByRestaurantIdAndRole(setup.getRestaurentId(), UserRole.OWNER);

    	if(dbUser.isPresent()) {
    		AuthUser user = dbUser.get();
    		user.setOnboardingStatus(OnboardingStatus.valueOf(setup.getOnboardingStatus()));
    		user.setSubscriptionPlan(setup.getPlan());
    		AuthUser savedUser =  userRepository.save(user);
    		if(savedUser.getOnboardingStatus().equals(OnboardingStatus.valueOf(setup.getOnboardingStatus()))) {
    			resp.setMessage("success");
    			resp.setStatus(true);
    			return resp;
    		}
    	}
    	}catch(Exception e) {
    		return resp;
    	}
		return resp;

    }

    public MessageResponse upgradePlan(UpgradePlan plan) {

    	MessageResponse response = new MessageResponse("failure", false);

    	Optional<List<AuthUser>> user = userRepository.findByRestaurantId(plan.getRestaurantId());
    	if(user.isPresent()) {
    		for(AuthUser authUser : user.get()) {
    			authUser.setSubscriptionPlan(plan.getPlan());
    			try {
    				userRepository.save(authUser);
    			}catch(Exception e) {
    				return response;
    			}
    		}
    		response.setMessage("success");
    		response.setStatus(true);
    	}
    	return response;
    }


}
