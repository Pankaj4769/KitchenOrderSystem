package com.kos.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentWebhookController {

    private static final Logger logger = LogManager.getLogger(PaymentWebhookController.class);

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        logger.info("Entering handleWebhook()");
        try {
            // TODO: verify HMAC-SHA256 signature using razorpay.webhook.secret when Razorpay is active
            // For now, return 200 OK for the mock gateway
            ResponseEntity<String> result = ResponseEntity.ok("ok");
            logger.info("Exiting handleWebhook()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in handleWebhook(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
