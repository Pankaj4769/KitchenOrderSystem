package com.kos.dto;

public class PeakHours {
    private String busiest;
    private String quietest;
    private String currentStatus;
    
    
    
	public String getBusiest() {
		return busiest;
	}
	public void setBusiest(String busiest) {
		this.busiest = busiest;
	}
	public String getQuietest() {
		return quietest;
	}
	public void setQuietest(String quietest) {
		this.quietest = quietest;
	}
	public String getCurrentStatus() {
		return currentStatus;
	}
	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}
    
    
}
