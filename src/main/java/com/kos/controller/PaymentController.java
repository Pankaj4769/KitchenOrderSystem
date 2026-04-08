package com.kos.controller;

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
	
	@Autowired
	PaymentService paymentService;
	
	@PatchMapping("/doPayment")
	public ResponseEntity<PaymentResponse> doPayment(@RequestBody PaymentRequest paymentReq){
		
		return ResponseEntity.ok(paymentService.doPayment(paymentReq));
	}
	
	
	@PostMapping("/doBillPayment")
	public ResponseEntity<MessageResponse> doBillPayment(@RequestBody PaymentData req){
		return ResponseEntity.ok(paymentService.processPayment(req));
	}
	

}
