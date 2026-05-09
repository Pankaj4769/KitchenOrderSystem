package com.kos.dto;

import java.util.List;
import java.util.Map;

public class SalesReportResponse {
    
    private SalesKpi metrics;
    private List<PaymentBreakdown> paymentBreakdown;
    private List<OrderTypeBreakdown> orderTypeBreakdown;
    private List<TopSellingItem> topSellingItems;
    private List<StaffSales> staffSales;
    private List<HourlyRevenue> revenueByHour;
    private String restaurantName;
    private String restaurantAddress;

    // Getters and Setters
    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getRestaurantAddress() { return restaurantAddress; }
    public void setRestaurantAddress(String restaurantAddress) { this.restaurantAddress = restaurantAddress; }

    // Getters and Setters
    public SalesKpi getMetrics() { return metrics; }
    public void setMetrics(SalesKpi metrics) { this.metrics = metrics; }

    public List<PaymentBreakdown> getPaymentBreakdown() { return paymentBreakdown; }
    public void setPaymentBreakdown(List<PaymentBreakdown> paymentBreakdown) { this.paymentBreakdown = paymentBreakdown; }

    public List<OrderTypeBreakdown> getOrderTypeBreakdown() { return orderTypeBreakdown; }
    public void setOrderTypeBreakdown(List<OrderTypeBreakdown> orderTypeBreakdown) { this.orderTypeBreakdown = orderTypeBreakdown; }

    public List<TopSellingItem> getTopSellingItems() { return topSellingItems; }
    public void setTopSellingItems(List<TopSellingItem> topSellingItems) { this.topSellingItems = topSellingItems; }

    public List<StaffSales> getStaffSales() { return staffSales; }
    public void setStaffSales(List<StaffSales> staffSales) { this.staffSales = staffSales; }

    public List<HourlyRevenue> getRevenueByHour() { return revenueByHour; }
    public void setRevenueByHour(List<HourlyRevenue> revenueByHour) { this.revenueByHour = revenueByHour; }

    // Nested DTOs
    public static class SalesKpi {
        private Double grossSales;
        private Double netSales;
        private Long totalOrders;
        private Long completedOrders;
        private Long cancelledOrders;
        private Double avgOrderValue;
        private Double taxCollected;

        public Double getGrossSales() { return grossSales; }
        public void setGrossSales(Double grossSales) { this.grossSales = grossSales; }
        public Double getNetSales() { return netSales; }
        public void setNetSales(Double netSales) { this.netSales = netSales; }
        public Long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
        public Long getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(Long completedOrders) { this.completedOrders = completedOrders; }
        public Long getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(Long cancelledOrders) { this.cancelledOrders = cancelledOrders; }
        public Double getAvgOrderValue() { return avgOrderValue; }
        public void setAvgOrderValue(Double avgOrderValue) { this.avgOrderValue = avgOrderValue; }
        public Double getTaxCollected() { return taxCollected; }
        public void setTaxCollected(Double taxCollected) { this.taxCollected = taxCollected; }
    }

    public static class PaymentBreakdown {
        private String method;
        private Long orderCount;
        private Double amount;
        private Double percentage;

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public Long getOrderCount() { return orderCount; }
        public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }

    public static class OrderTypeBreakdown {
        private String type;
        private Long count;
        private Double revenue;
        private Double percentage;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }

    public static class StaffSales {
        private String name;
        private Long ordersHandled;
        private Double revenue;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getOrdersHandled() { return ordersHandled; }
        public void setOrdersHandled(Long ordersHandled) { this.ordersHandled = ordersHandled; }
        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }
    }

    public static class HourlyRevenue {
        private Integer hour;
        private Double amount;

        public Integer getHour() { return hour; }
        public void setHour(Integer hour) { this.hour = hour; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }

    public static class TopSellingItem {
        private String name;
        private String category;
        private Long quantitySold;
        private Double grossAmount;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Long getQuantitySold() { return quantitySold; }
        public void setQuantitySold(Long quantitySold) { this.quantitySold = quantitySold; }
        public Double getGrossAmount() { return grossAmount; }
        public void setGrossAmount(Double grossAmount) { this.grossAmount = grossAmount; }
    }
}
