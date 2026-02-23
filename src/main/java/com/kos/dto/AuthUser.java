package com.kos.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.annotation.Nullable; // optional

@Entity
public class AuthUser {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
    private Integer staffId;
    private String name;
    
    @Column(nullable = false, unique = true)
    private String username;

    @Nullable
    private String email;

    @Nullable
    private String mobile;

    private UserRole role;
    private String token;
    private boolean isFirstTime;
    private OnboardingStatus onboardingStatus;

    @Nullable
    private SubscriptionPlan subscriptionPlan;

    @Nullable
    private String restaurantId;

	public Integer getStaffId() {
		return staffId;
	}

	public void setStaffId(Integer staffId) {
		this.staffId = staffId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isFirstTime() {
		return isFirstTime;
	}

	public void setFirstTime(boolean isFirstTime) {
		this.isFirstTime = isFirstTime;
	}

	public OnboardingStatus getOnboardingStatus() {
		return onboardingStatus;
	}

	public void setOnboardingStatus(OnboardingStatus onboardingStatus) {
		this.onboardingStatus = onboardingStatus;
	}

	public SubscriptionPlan getSubscriptionPlan() {
		return subscriptionPlan;
	}

	public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
		this.subscriptionPlan = subscriptionPlan;
	}

	public String getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(String restaurantId) {
		this.restaurantId = restaurantId;
	}
    
    

}
