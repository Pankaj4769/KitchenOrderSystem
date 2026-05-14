package com.kos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentWebhookController {

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        // TODO: verify HMAC-SHA256 signature using razorpay.webhook.secret when Razorpay is active
        // For now, return 200 OK for the mock gateway
        return ResponseEntity.ok("ok");
    }
}
