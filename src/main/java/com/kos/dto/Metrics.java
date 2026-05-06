package com.kos.dto;

public class Metrics {
    private long totalOrders;
    private double totalRevenue;
    private double avgOrderValue;
    private long completedOrders;
    private double growthPercentage;
    
    
	public long getTotalOrders() {
		return totalOrders;
	}
	public void setTotalOrders(long totalOrders) {
		this.totalOrders = totalOrders;
	}
	public double getTotalRevenue() {
		return totalRevenue;
	}
	public void setTotalRevenue(double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}
	public double getAvgOrderValue() {
		return avgOrderValue;
	}
	public void setAvgOrderValue(double avgOrderValue) {
		this.avgOrderValue = avgOrderValue;
	}
	public long getCompletedOrders() {
		return completedOrders;
	}
	public void setCompletedOrders(long completedOrders) {
		this.completedOrders = completedOrders;
	}
	public double getGrowthPercentage() {
		return growthPercentage;
	}
	public void setGrowthPercentage(double growthPercentage) {
		this.growthPercentage = growthPercentage;
	}
    
    
}
