package com.kos.payment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "payment.gateway", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGateway implements PaymentGatewayPort {

    @Override
    public MandateResult createMandate(long restaurantId, String planName, double amount) {
        String mandateId = "MOCK-" + restaurantId + "-" + System.currentTimeMillis();
        return new MandateResult(mandateId, true, "Mock mandate created");
    }

    @Override
    public ChargeResult chargeMandate(String mandateId, double amount) {
        String txnId = "MOCK-TXN-" + System.currentTimeMillis();
        return new ChargeResult(true, txnId, "Mock charge successful");
    }

    @Override
    public void cancelMandate(String mandateId) {
        // no-op for mock
    }
}
