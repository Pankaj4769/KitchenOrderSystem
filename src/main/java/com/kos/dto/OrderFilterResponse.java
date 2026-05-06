package com.kos.dto;

import java.util.List;

public class OrderFilterResponse {

    private List<Order> orders;
    private long totalElements;
    private int page;
    private int size;
    private int totalPages;

    public OrderFilterResponse() {}

    public OrderFilterResponse(List<Order> orders, long totalElements, int page, int size) {
        this.orders = orders;
        this.totalElements = totalElements;
        this.page = page;
        this.size = size;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
