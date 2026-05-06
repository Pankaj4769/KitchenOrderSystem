package com.kos.dto;

import java.time.LocalDateTime;

public class OccupyTableRequest {

    private String orderNumber;
    private String waiter;
    private LocalDateTime startTime;

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getWaiter() { return waiter; }
    public void setWaiter(String waiter) { this.waiter = waiter; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
}
