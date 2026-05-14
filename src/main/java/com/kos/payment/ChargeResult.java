package com.kos.payment;

public record ChargeResult(boolean success, String transactionId, String message) {}
