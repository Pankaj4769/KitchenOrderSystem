package com.kos.payment;

public interface PaymentGatewayPort {
    MandateResult createMandate(long restaurantId, String planName, double amount);
    ChargeResult  chargeMandate(String mandateId, double amount);
    void          cancelMandate(String mandateId);
}
