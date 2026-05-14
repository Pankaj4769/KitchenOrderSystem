package com.kos.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "payment.gateway", havingValue = "razorpay")
public class RazorpayPaymentGateway implements PaymentGatewayPort {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Override
    public MandateResult createMandate(long restaurantId, String planName, double amount) {
        // TODO: implement with Razorpay SDK
        // RazorpayClient client = new RazorpayClient(keyId, keySecret);
        // JSONObject subscriptionOptions = new JSONObject();
        // subscriptionOptions.put("plan_id", planName);
        // subscriptionOptions.put("customer_notify", 1);
        // subscriptionOptions.put("total_count", 12);
        // Subscription subscription = client.subscriptions.create(subscriptionOptions);
        // return new MandateResult(subscription.get("id"), true, "Mandate created");
        throw new UnsupportedOperationException("Razorpay not yet configured — provide API keys in application.properties");
    }

    @Override
    public ChargeResult chargeMandate(String mandateId, double amount) {
        // TODO: implement with Razorpay SDK
        // RazorpayClient client = new RazorpayClient(keyId, keySecret);
        // JSONObject paymentOptions = new JSONObject();
        // paymentOptions.put("subscription_id", mandateId);
        // paymentOptions.put("amount", (int)(amount * 100));
        // paymentOptions.put("currency", "INR");
        // Payment payment = client.payments.capture(paymentOptions);
        // return new ChargeResult(true, payment.get("id"), "Charged successfully");
        throw new UnsupportedOperationException("Razorpay not yet configured — provide API keys in application.properties");
    }

    @Override
    public void cancelMandate(String mandateId) {
        // TODO: implement with Razorpay SDK
        // RazorpayClient client = new RazorpayClient(keyId, keySecret);
        // client.subscriptions.cancel(mandateId, new JSONObject());
        throw new UnsupportedOperationException("Razorpay not yet configured — provide API keys in application.properties");
    }
}
