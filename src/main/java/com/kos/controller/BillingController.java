package com.kos.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.kos.dto.Bill;
import com.kos.service.BillingService;

@RestController
public class BillingController {

    private static final Logger logger = LogManager.getLogger(BillingController.class);

    @Autowired
    BillingService billingService;

    @GetMapping("/generateBill/{orderId}")
    public ResponseEntity<Bill> generateBill(@PathVariable Integer orderId) {
        logger.info("Entering generateBill() with orderId={}", orderId);
        try {
            ResponseEntity<Bill> result = new ResponseEntity<Bill>(
                    billingService.generateBill(orderId),
                    HttpStatus.OK
            );
            logger.info("Exiting generateBill()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in generateBill(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
