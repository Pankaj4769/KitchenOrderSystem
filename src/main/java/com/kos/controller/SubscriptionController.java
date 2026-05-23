package com.kos.controller;

import com.kos.dto.CompleteSetup;
import com.kos.dto.MessageResponse;
import com.kos.dto.StartTrialRequestDTO;
import com.kos.dto.SubscriptionRequestDTO;
import com.kos.dto.SubscriptionResponseDTO;
import com.kos.dto.UpgradePlan;
import com.kos.model.SubscriptionPlan.PlanType;
import com.kos.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

	private static final Logger logger = LogManager.getLogger(SubscriptionController.class);

	@Autowired
	SubscriptionService subscriptionService;

    @PostMapping("/assign")
    public ResponseEntity<SubscriptionResponseDTO> assignPlan(
            @Valid @RequestBody SubscriptionRequestDTO request) {
        logger.info("Entering assignPlan()");
        try {
            ResponseEntity<SubscriptionResponseDTO> result = ResponseEntity.ok(subscriptionService.assignPlan(request));
            logger.info("Exiting assignPlan()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in assignPlan(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/upgrade/{restaurantId}")
    public ResponseEntity<SubscriptionResponseDTO> upgradePlan(
            @PathVariable Long restaurantId,
            @RequestParam PlanType newPlan) {
        logger.info("Entering upgradePlan() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<SubscriptionResponseDTO> result = ResponseEntity.ok(subscriptionService.upgradePlan(restaurantId, newPlan));
            logger.info("Exiting upgradePlan()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in upgradePlan(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<SubscriptionResponseDTO> getSubscription(
            @PathVariable Long restaurantId) {
        logger.info("Entering getSubscription() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<SubscriptionResponseDTO> result = ResponseEntity.ok(subscriptionService.getSubscription(restaurantId));
            logger.info("Exiting getSubscription()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getSubscription(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionResponseDTO>> getAllPlans() {
        logger.info("Entering getAllPlans()");
        try {
            ResponseEntity<List<SubscriptionResponseDTO>> result = ResponseEntity.ok(subscriptionService.getAllPlans());
            logger.info("Exiting getAllPlans()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getAllPlans(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/cancel/{restaurantId}")
    public ResponseEntity<String> cancelSubscription(
            @PathVariable Long restaurantId) {
        logger.info("Entering cancelSubscription() with restaurantId={}", restaurantId);
        try {
            subscriptionService.cancelSubscription(restaurantId);
            logger.info("Exiting cancelSubscription()");
            return ResponseEntity.ok("Subscription cancelled successfully");
        } catch (RuntimeException e) {
            logger.error("Error in cancelSubscription(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @PatchMapping("/completeSetup")
    public ResponseEntity<MessageResponse> updateOnboardingStatus(@RequestBody CompleteSetup setup){
    	logger.info("Entering updateOnboardingStatus()");
    	try {
    		ResponseEntity<MessageResponse> result = ResponseEntity.ok(subscriptionService.updateOnboardingStatus(setup));
    		logger.info("Exiting updateOnboardingStatus()");
    		return result;
    	} catch (RuntimeException e) {
    		logger.error("Error in updateOnboardingStatus(): {}", e.getMessage(), e);
    		throw e;
    	}
    }

    @PatchMapping("/upgradePlan")
    public ResponseEntity<MessageResponse> upgradePlan(@RequestBody UpgradePlan plan){
    	logger.info("Entering upgradePlan()");
    	try {
    		ResponseEntity<MessageResponse> result = ResponseEntity.ok(subscriptionService.upgradePlan(plan));
    		logger.info("Exiting upgradePlan()");
    		return result;
    	} catch (RuntimeException e) {
    		logger.error("Error in upgradePlan(): {}", e.getMessage(), e);
    		throw e;
    	}
    }

    @PostMapping("/startTrial")
    public ResponseEntity<SubscriptionResponseDTO> startTrial(@RequestBody StartTrialRequestDTO request) {
        logger.info("Entering startTrial()");
        try {
            ResponseEntity<SubscriptionResponseDTO> result = ResponseEntity.ok(subscriptionService.startTrial(request));
            logger.info("Exiting startTrial()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in startTrial(): {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/completeTrial/{restaurantId}")
    public ResponseEntity<Map<String, Object>> completeTrial(@PathVariable Long restaurantId) {
        logger.info("Entering completeTrial() with restaurantId={}", restaurantId);
        try {
            ResponseEntity<Map<String, Object>> result = ResponseEntity.ok(subscriptionService.completeTrial(restaurantId));
            logger.info("Exiting completeTrial()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in completeTrial(): {}", e.getMessage(), e);
            throw e;
        }
    }

}
