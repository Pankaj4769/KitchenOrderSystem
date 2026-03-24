package com.kos.dto;

public class CompleteSetup {
	
	private String onboardingStatus;
	private String restaurentId;
	private RestaurantSetup restaurant ;
	private SubscriptionPlan plan;
	
	public String getOnboardingStatus() {
		return onboardingStatus;
	}
	public void setOnboardingStatus(String onboardingStatus) {
		this.onboardingStatus = onboardingStatus;
	}
	public RestaurantSetup getRestaurant() {
		return restaurant;
	}
	public void setRestaurant(RestaurantSetup restaurant) {
		this.restaurant = restaurant;
	}
	public SubscriptionPlan getPlan() {
		return plan;
	}
	public void setPlan(SubscriptionPlan plan) {
		this.plan = plan;
	}
	public String getRestaurentId() {
		return restaurentId;
	}
	public void setRestaurentId(String restaurentId) {
		this.restaurentId = restaurentId;
	}
	

}
