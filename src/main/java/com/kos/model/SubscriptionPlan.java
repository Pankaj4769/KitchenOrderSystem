package com.kos.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "subscription_plans")
@Data
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanType planName;  // STARTER, GROWTH, PRO, ENTERPRISE

    private String description;
    private Double price;
    private int durationDays;   // 30, 90, 365

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plan_features", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature")
    private List<String> allowedFeatures;

    public enum PlanType {
        STARTER,
        GROWTH,
        PRO,
        ENTERPRISE
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PlanType getPlanName() {
		return planName;
	}

	public void setPlanName(PlanType planName) {
		this.planName = planName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public int getDurationDays() {
		return durationDays;
	}

	public void setDurationDays(int durationDays) {
		this.durationDays = durationDays;
	}

	public List<String> getAllowedFeatures() {
		return allowedFeatures;
	}

	public void setAllowedFeatures(List<String> allowedFeatures) {
		this.allowedFeatures = allowedFeatures;
	}
    
}
