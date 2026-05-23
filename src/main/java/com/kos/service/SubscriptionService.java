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
import com.kos.repository.RestaurentRepository;
import com.kos.repository.SubscriptionPlanRepository;
import com.kos.repository.SubscriptionRepository;
import com.kos.repository.UserRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

	private static final Logger logger = LogManager.getLogger(SubscriptionService.class);

	@Autowired
	SubscriptionRepository subscriptionRepository;
	@Autowired
	SubscriptionPlanRepository planRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RestaurentRepository restaurentRepository;

	@Autowired
	UserService userService;

	// Change 1: Inject PaymentGatewayPort and trial duration config
	@Autowired
	private PaymentGatewayPort paymentGateway;

	@Value("${subscription.trial.duration-days:14}")
	private int trialDurationDays;

    public SubscriptionResponseDTO assignPlan(SubscriptionRequestDTO request) {
        logger.info("Entering assignPlan()");
        try {
            subscriptionRepository
                .findByRestaurantIdAndStatusIn(request.getRestaurantId(),
                    List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL))
                .ifPresent(s -> { throw new RuntimeException(
                    "Restaurant " + request.getRestaurantId() +
                    " already has an active or trial subscription. Use upgrade instead."); });

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

            SubscriptionResponseDTO result = mapToDTO(subscriptionRepository.save(subscription));
            logger.info("Exiting assignPlan()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in assignPlan(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // Change 3: Fix upgradePlan(Long, PlanType) to include TRIAL + swap mandate when TRIAL
    public SubscriptionResponseDTO upgradePlan(Long restaurantId, PlanType newPlanType) {
        logger.info("Entering upgradePlan() with restaurantId={}", restaurantId);
        try {
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

            SubscriptionResponseDTO result = mapToDTO(subscriptionRepository.save(existing));
            logger.info("Exiting upgradePlan()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in upgradePlan(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // Change 2: Fix getSubscription to include TRIAL
    public SubscriptionResponseDTO getSubscription(Long restaurantId) {
        logger.info("Entering getSubscription() with restaurantId={}", restaurantId);
        try {
            Subscription subscription = subscriptionRepository
                    .findByRestaurantIdAndStatusIn(restaurantId, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL))
                    .orElseThrow(() -> new RuntimeException("No active or trial subscription for restaurant " + restaurantId));
            SubscriptionResponseDTO result = mapToDTO(subscription);
            logger.info("Exiting getSubscription()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getSubscription(): {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<SubscriptionResponseDTO> getAllPlans() {
        logger.info("Entering getAllPlans()");
        try {
            List<SubscriptionResponseDTO> result = planRepository.findAll()
                    .stream()
                    .map(this::mapPlanToDTO)
                    .collect(Collectors.toList());
            logger.info("Exiting getAllPlans()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getAllPlans(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // Change 4: Fix cancelSubscription to include TRIAL + cancel mandate
    public void cancelSubscription(Long restaurantId) {
        logger.info("Entering cancelSubscription() with restaurantId={}", restaurantId);
        try {
            Subscription sub = subscriptionRepository
                    .findByRestaurantIdAndStatusIn(restaurantId, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL))
                    .orElseThrow(() -> new RuntimeException("No active or trial subscription found"));
            if (sub.getMandateId() != null) {
                paymentGateway.cancelMandate(sub.getMandateId());
            }
            sub.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(sub);
            logger.info("Exiting cancelSubscription()");
        } catch (RuntimeException e) {
            logger.error("Error in cancelSubscription(): {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean hasFeatureAccess(Long restaurantId, String feature) {
        logger.info("Entering hasFeatureAccess() with restaurantId={}", restaurantId);
        try {
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
            logger.info("Exiting hasFeatureAccess()");
            return true;
        } catch (RuntimeException e) {
            logger.error("Error in hasFeatureAccess(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // Change 7: Extended scheduler to also auto-charge expired TRIAL subscriptions
    @Scheduled(cron = "0 0 0 * * *")  // runs every midnight
    public void checkAndExpireSubscriptions() {
        logger.info("Entering checkAndExpireSubscriptions()");
        try {
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
            logger.info("Exiting checkAndExpireSubscriptions()");
        } catch (RuntimeException e) {
            logger.error("Error in checkAndExpireSubscriptions(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // Change 5: Add startTrial() method
    @Transactional
    public SubscriptionResponseDTO startTrial(StartTrialRequestDTO request) {
        logger.info("Entering startTrial()");
        try {
            Long restaurantId = request.getRestaurantId();

            // Guard: one trial per restaurant, ever
            boolean hasHistory = subscriptionRepository.existsByRestaurantIdAndStatusNot(
                restaurantId, SubscriptionStatus.CANCELLED);
            if (hasHistory) {
                String restaurantName = restaurentRepository.findById(restaurantId.intValue())
                    .map(com.kos.dto.Restaurent::getRestaurentName)
                    .orElse(null);
                throw new TrialAlreadyUsedException(restaurantName, restaurantId);
            }

            // Fetch plan or create it using the price/duration the UI passed
            PlanType planType = PlanType.valueOf(request.getPlanName());
            SubscriptionPlan plan = planRepository.findByPlanName(planType)
                .orElseGet(() -> {
                    SubscriptionPlan newPlan = new SubscriptionPlan();
                    newPlan.setPlanName(planType);
                    newPlan.setPrice(request.getPrice());
                    newPlan.setDurationDays(request.getDurationDays() > 0 ? request.getDurationDays() : 30);
                    return planRepository.save(newPlan);
                });

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

            // Update all AuthUsers for this restaurant to reflect the selected plan
            com.kos.dto.SubscriptionPlan userPlan =
                com.kos.dto.SubscriptionPlan.valueOf(plan.getPlanName().name());
            userRepository.findByRestaurantId(String.valueOf(restaurantId))
                .ifPresent(users -> users.forEach(u -> {
                    u.setSubscriptionPlan(userPlan);
                    userRepository.save(u);
                }));

            SubscriptionResponseDTO result = mapToDTO(sub);
            logger.info("Exiting startTrial()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in startTrial(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // Change 6: Add completeTrial() method
    @Transactional
    public Map<String, Object> completeTrial(Long restaurantId) {
        logger.info("Entering completeTrial() with restaurantId={}", restaurantId);
        try {
            Subscription sub = subscriptionRepository
                .findByRestaurantIdAndStatusIn(restaurantId,
                    List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL))
                .orElseThrow(() -> new RuntimeException("No active or trial subscription for restaurant " + restaurantId));

            if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
                logger.info("Exiting completeTrial()");
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
                logger.info("Exiting completeTrial()");
                return Map.of("alreadyConverted", false, "status", "ACTIVE");
            } else {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(sub);
                sendExpiryEmail(sub);
                logger.info("Exiting completeTrial()");
                return Map.of("alreadyConverted", false, "status", "EXPIRED", "error", charge.message());
            }
        } catch (RuntimeException e) {
            logger.error("Error in completeTrial(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // Change 8: sendExpiryEmail helper (stub — wire actual email service if available)
    private void sendExpiryEmail(Subscription sub) {
        logger.info("Entering sendExpiryEmail()");
        try {
            // Wire to existing email/SMTP service when available
            // e.g., emailService.sendExpiryNotification(sub.getRestaurantId(), sub.getExpiryDate());
            logger.info("Exiting sendExpiryEmail()");
        } catch (Exception e) {
            logger.error("Failed to send expiry email for restaurant {}: {}", sub.getRestaurantId(), e.getMessage(), e);
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
    	logger.info("Entering updateOnboardingStatus()");
    	try {
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
	    			logger.info("Exiting updateOnboardingStatus()");
	    			return resp;
	    		}
	    	}
	    	}catch(Exception e) {
	    		logger.error("Error during updateOnboardingStatus inner block: {}", e.getMessage(), e);
	    		logger.info("Exiting updateOnboardingStatus()");
	    		return resp;
	    	}
	    	logger.info("Exiting updateOnboardingStatus()");
			return resp;
    	} catch (RuntimeException e) {
    		logger.error("Error in updateOnboardingStatus(): {}", e.getMessage(), e);
    		throw e;
    	}
    }

    /**
     * Activates the restaurant's subscription after a successful payment:
     * promotes any TRIAL / ACTIVE / EXPIRED row to ACTIVE, refreshes the
     * start/expiry dates from the chosen plan's duration, and clears the
     * trial end date. Called by PaymentService.doPayment.
     */
    @Transactional
    public void activateSubscriptionAfterPayment(Long restaurantId, String planName) {
        logger.info("Entering activateSubscriptionAfterPayment() with restaurantId={}", restaurantId);
        try {
            if (restaurantId == null) {
                logger.info("Exiting activateSubscriptionAfterPayment()");
                return;
            }

            Optional<Subscription> subOpt = subscriptionRepository
                .findByRestaurantIdAndStatusIn(restaurantId,
                    List.of(SubscriptionStatus.TRIAL,
                            SubscriptionStatus.ACTIVE,
                            SubscriptionStatus.EXPIRED));
            if (subOpt.isEmpty()) {
                logger.info("Exiting activateSubscriptionAfterPayment()");
                return;
            }

            Subscription sub = subOpt.get();

            // If the caller passed a different plan, swap it in so the duration
            // calculation below uses the right plan.
            if (planName != null && !planName.isBlank()) {
                try {
                    PlanType planType = PlanType.valueOf(planName);
                    planRepository.findByPlanName(planType).ifPresent(sub::setPlan);
                } catch (IllegalArgumentException ignored) {
                    // unknown plan name — fall back to existing plan on the row
                }
            }

            int days = sub.getPlan() != null ? sub.getPlan().getDurationDays() : 0;
            if (days <= 0) days = 30;

            sub.setStatus(SubscriptionStatus.ACTIVE);
            sub.setStartDate(LocalDate.now());
            sub.setExpiryDate(LocalDate.now().plusDays(days));
            sub.setTrialEndDate(null);
            subscriptionRepository.save(sub);
            logger.info("Exiting activateSubscriptionAfterPayment()");
        } catch (RuntimeException e) {
            logger.error("Error in activateSubscriptionAfterPayment(): {}", e.getMessage(), e);
            throw e;
        }
    }

    public MessageResponse upgradePlan(UpgradePlan plan) {
        logger.info("Entering upgradePlan()");
        try {
	    	MessageResponse response = new MessageResponse("failure", false);

	    	Optional<List<AuthUser>> user = userRepository.findByRestaurantId(plan.getRestaurantId());
	    	if(user.isPresent()) {
	    		for(AuthUser authUser : user.get()) {
	    			authUser.setSubscriptionPlan(plan.getPlan());
	    			try {
	    				userRepository.save(authUser);
	    			}catch(Exception e) {
	    				logger.error("Error saving user in upgradePlan: {}", e.getMessage(), e);
	    				logger.info("Exiting upgradePlan()");
	    				return response;
	    			}
	    		}
	    		response.setMessage("success");
	    		response.setStatus(true);
	    	}
	    	logger.info("Exiting upgradePlan()");
	    	return response;
        } catch (RuntimeException e) {
            logger.error("Error in upgradePlan(): {}", e.getMessage(), e);
            throw e;
        }
    }


}
