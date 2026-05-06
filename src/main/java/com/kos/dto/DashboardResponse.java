package com.kos.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardResponse {
	
	private LocalDateTime lastUpdated;

    private Metrics metrics;
    private OrdersByStatus ordersByStatus;
    private DailyGoal dailyGoal;
    private AvgWaitTime avgWaitTime;

    private List<TopSellingItem> topSellingItems;
    private List<RevenueByHour> revenueByHour;
    private List<RecentOrder> recentOrders;

    private PeakHours peakHours;
    private TableStatus tableStatus;
    private OrderTypeStats orderTypeStats;

    private List<CategoryStats> categoryStats;
    private List<WaiterStats> waiterStats;

    private List<AlertDto> alerts;

    public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Metrics getMetrics() {
		return metrics;
	}

	public void setMetrics(Metrics metrics) {
		this.metrics = metrics;
	}

	public OrdersByStatus getOrdersByStatus() {
		return ordersByStatus;
	}

	public void setOrdersByStatus(OrdersByStatus ordersByStatus) {
		this.ordersByStatus = ordersByStatus;
	}

	public DailyGoal getDailyGoal() {
		return dailyGoal;
	}

	public void setDailyGoal(DailyGoal dailyGoal) {
		this.dailyGoal = dailyGoal;
	}

	public AvgWaitTime getAvgWaitTime() {
		return avgWaitTime;
	}

	public void setAvgWaitTime(AvgWaitTime avgWaitTime) {
		this.avgWaitTime = avgWaitTime;
	}

	public List<TopSellingItem> getTopSellingItems() {
		return topSellingItems;
	}

	public void setTopSellingItems(List<TopSellingItem> topSellingItems) {
		this.topSellingItems = topSellingItems;
	}

	public List<RevenueByHour> getRevenueByHour() {
		return revenueByHour;
	}

	public void setRevenueByHour(List<RevenueByHour> revenueByHour) {
		this.revenueByHour = revenueByHour;
	}

	public List<RecentOrder> getRecentOrders() {
		return recentOrders;
	}

	public void setRecentOrders(List<RecentOrder> recentOrders) {
		this.recentOrders = recentOrders;
	}

	public PeakHours getPeakHours() {
		return peakHours;
	}

	public void setPeakHours(PeakHours peakHours) {
		this.peakHours = peakHours;
	}

	public TableStatus getTableStatus() {
		return tableStatus;
	}

	public void setTableStatus(TableStatus tableStatus) {
		this.tableStatus = tableStatus;
	}

	public OrderTypeStats getOrderTypeStats() {
		return orderTypeStats;
	}

	public void setOrderTypeStats(OrderTypeStats orderTypeStats) {
		this.orderTypeStats = orderTypeStats;
	}

	public List<CategoryStats> getCategoryStats() {
		return categoryStats;
	}

	public void setCategoryStats(List<CategoryStats> categoryStats) {
		this.categoryStats = categoryStats;
	}

	public List<WaiterStats> getWaiterStats() {
		return waiterStats;
	}

	public void setWaiterStats(List<WaiterStats> waiterStats) {
		this.waiterStats = waiterStats;
	}

	public List<AlertDto> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<AlertDto> alerts) {
		this.alerts = alerts;
	}
    
    
}
