package com.kos.payment;

public record MandateResult(String mandateId, boolean success, String message) {}
