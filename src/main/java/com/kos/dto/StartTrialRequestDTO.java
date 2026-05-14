package com.kos.dto;

public class StartTrialRequestDTO {
    private Long restaurantId;
    private String planName;
    private PaymentDetailsDTO paymentDetails;

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public PaymentDetailsDTO getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(PaymentDetailsDTO paymentDetails) { this.paymentDetails = paymentDetails; }
}
