package com.kos.dto;

import com.kos.model.SubscriptionPlan.PlanType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionRequestDTO {

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotNull(message = "Plan type is required")
    private PlanType planName;   // STARTER, GROWTH, PRO, ENTERPRISE

    private int durationDays;    // optional override, default from plan

	public Long getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(Long restaurantId) {
		this.restaurantId = restaurantId;
	}

	public PlanType getPlanName() {
		return planName;
	}

	public void setPlanName(PlanType planName) {
		this.planName = planName;
	}

	public int getDurationDays() {
		return durationDays;
	}

	public void setDurationDays(int durationDays) {
		this.durationDays = durationDays;
	}
    
}
