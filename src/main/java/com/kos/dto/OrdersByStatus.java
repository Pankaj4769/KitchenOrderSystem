package com.kos.dto;


public class OrdersByStatus {
    private long pending;
    private long preparing;
    private long ready;
    
    
	public long getPending() {
		return pending;
	}
	public void setPending(long pending) {
		this.pending = pending;
	}
	public long getPreparing() {
		return preparing;
	}
	public void setPreparing(long preparing) {
		this.preparing = preparing;
	}
	public long getReady() {
		return ready;
	}
	public void setReady(long ready) {
		this.ready = ready;
	}
    
    
}
