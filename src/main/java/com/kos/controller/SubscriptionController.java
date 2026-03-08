package com.kos.controller;

import com.kos.dto.SubscriptionRequestDTO;
import com.kos.dto.SubscriptionResponseDTO;
import com.kos.model.SubscriptionPlan.PlanType;
import com.kos.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	@Autowired
	public SubscriptionController(SubscriptionService subscriptionService) {
	    this.subscriptionService = subscriptionService;
	}

    @PostMapping("/assign")
    public ResponseEntity<SubscriptionResponseDTO> assignPlan(
            @Valid @RequestBody SubscriptionRequestDTO request) {
        return ResponseEntity.ok(subscriptionService.assignPlan(request));
    }

    @PutMapping("/upgrade/{restaurantId}")
    public ResponseEntity<SubscriptionResponseDTO> upgradePlan(
            @PathVariable Long restaurantId,
            @RequestParam PlanType newPlan) {
        return ResponseEntity.ok(subscriptionService.upgradePlan(restaurantId, newPlan));
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<SubscriptionResponseDTO> getSubscription(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(subscriptionService.getSubscription(restaurantId));
    }

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionResponseDTO>> getAllPlans() {
        return ResponseEntity.ok(subscriptionService.getAllPlans());
    }

    @DeleteMapping("/cancel/{restaurantId}")
    public ResponseEntity<String> cancelSubscription(
            @PathVariable Long restaurantId) {
        subscriptionService.cancelSubscription(restaurantId);
        return ResponseEntity.ok("Subscription cancelled successfully");
    }
}
