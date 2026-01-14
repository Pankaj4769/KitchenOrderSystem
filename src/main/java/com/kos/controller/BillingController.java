package com.kos.controller;

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

    @Autowired
    BillingService billingService;

    @GetMapping("/generateBill/{orderId}")
    public ResponseEntity<Bill> generateBill(@PathVariable Integer orderId) {

        return new ResponseEntity<Bill>(
                billingService.generateBill(orderId),
                HttpStatus.OK
        );
    }
}
