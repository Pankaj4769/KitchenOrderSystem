package com.kos.dto;

public class PaymentResponse {

	private boolean paymentStatus;
	private String activePlan;
	
	public boolean getPaymentStatus() {
		return paymentStatus;
	}
	public void setPaymentStatus(boolean paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	public String getActivePlan() {
		return activePlan;
	}
	public void setActivePlan(String activePlan) {
		this.activePlan = activePlan;
	}
	
	
}
