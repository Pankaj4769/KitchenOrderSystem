package com.kos.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kos.dto.MessageResponse;
import com.kos.dto.PaymentData;
import com.kos.dto.PaymentRequest;
import com.kos.dto.PaymentResponse;
import com.kos.service.PaymentService;

@RestController
public class PaymentController {

	private static final Logger logger = LogManager.getLogger(PaymentController.class);

	@Autowired
	PaymentService paymentService;

	@PatchMapping("/doPayment")
	public ResponseEntity<PaymentResponse> doPayment(@RequestBody PaymentRequest paymentReq){
		logger.info("Entering doPayment()");
		try {
			ResponseEntity<PaymentResponse> result = ResponseEntity.ok(paymentService.doPayment(paymentReq));
			logger.info("Exiting doPayment()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in doPayment(): {}", e.getMessage(), e);
			throw e;
		}
	}


	@PostMapping("/doBillPayment")
	public ResponseEntity<MessageResponse> doBillPayment(@RequestBody PaymentData req){
		logger.info("Entering doBillPayment()");
		try {
			ResponseEntity<MessageResponse> result = ResponseEntity.ok(paymentService.processPayment(req));
			logger.info("Exiting doBillPayment()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in doBillPayment(): {}", e.getMessage(), e);
			throw e;
		}
	}


}
