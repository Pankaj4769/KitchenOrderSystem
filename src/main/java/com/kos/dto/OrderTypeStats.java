package com.kos.dto;

public class OrderTypeStats {
    private TypeStats dineIn;
    private TypeStats takeaway;
    private TypeStats delivery;
    
    
	public TypeStats getDineIn() {
		return dineIn;
	}
	public void setDineIn(TypeStats dineIn) {
		this.dineIn = dineIn;
	}
	public TypeStats getTakeaway() {
		return takeaway;
	}
	public void setTakeaway(TypeStats takeaway) {
		this.takeaway = takeaway;
	}
	public TypeStats getDelivery() {
		return delivery;
	}
	public void setDelivery(TypeStats delivery) {
		this.delivery = delivery;
	}
    
    
}