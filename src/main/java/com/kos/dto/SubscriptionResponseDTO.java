package com.kos.dto;

import com.kos.model.Subscription.SubscriptionStatus;
import com.kos.model.SubscriptionPlan.PlanType;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SubscriptionResponseDTO {

    private Long subscriptionId;
    private Long restaurantId;
    private PlanType planName;
    private Double price;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private SubscriptionStatus status;
    private List<String> allowedFeatures;
    private long daysRemaining;
	public Long getSubscriptionId() {
		return subscriptionId;
	}
	public void setSubscriptionId(Long subscriptionId) {
		this.subscriptionId = subscriptionId;
	}
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
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public LocalDate getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}
	public LocalDate getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(LocalDate expiryDate) {
		this.expiryDate = expiryDate;
	}
	public SubscriptionStatus getStatus() {
		return status;
	}
	public void setStatus(SubscriptionStatus status) {
		this.status = status;
	}
	public List<String> getAllowedFeatures() {
		return allowedFeatures;
	}
	public void setAllowedFeatures(List<String> allowedFeatures) {
		this.allowedFeatures = allowedFeatures;
	}
	public long getDaysRemaining() {
		return daysRemaining;
	}
	public void setDaysRemaining(long daysRemaining) {
		this.daysRemaining = daysRemaining;
	}
    
    
    
}
