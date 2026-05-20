package com.kos.dto;

public class StartTrialRequestDTO {
    private Long restaurantId;
    private String planName;
    private Double price;
    private int durationDays;
    private PaymentDetailsDTO paymentDetails;

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public PaymentDetailsDTO getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(PaymentDetailsDTO paymentDetails) { this.paymentDetails = paymentDetails; }
}
