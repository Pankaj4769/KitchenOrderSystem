package com.kos.dto;

public class OrderStatistics {

    private int totalOrders;
    private double totalRevenue;
    private double avgOrderValue;
    private int completedOrders;
    private int cancelledOrders;

    public OrderStatistics() {}

    public OrderStatistics(int totalOrders, double totalRevenue, double avgOrderValue,
                           int completedOrders, int cancelledOrders) {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.avgOrderValue = avgOrderValue;
        this.completedOrders = completedOrders;
        this.cancelledOrders = cancelledOrders;
    }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getAvgOrderValue() { return avgOrderValue; }
    public void setAvgOrderValue(double avgOrderValue) { this.avgOrderValue = avgOrderValue; }

    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }

    public int getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(int cancelledOrders) { this.cancelledOrders = cancelledOrders; }
}
