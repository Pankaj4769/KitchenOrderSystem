package com.kos.dto;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_data")
public class PaymentData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode mode;

    @Column(nullable = false)
    private double amount;

    private Double tip;

    private Double discount;

    @Column(name = "split_count")
    private Integer splitCount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payment_data_id") // FK in child table
    private List<PartPaymentEntry> partPayments;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(nullable = false)
    private Instant timestamp;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payment_data_id")
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "restaurant_id", nullable = false)
    private String restaurantId;

    // Constructors
    public PaymentData() {
    }

    public PaymentData(PaymentMethod method, PaymentMode mode, double amount,
                       Instant timestamp, List<CartItem> items, String restaurantId) {
        this.method = method;
        this.mode = mode;
        this.amount = amount;
        this.timestamp = timestamp;
        this.items = items;
        this.restaurantId = restaurantId;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentMode getMode() {
        return mode;
    }

    public void setMode(PaymentMode mode) {
        this.mode = mode;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Double getTip() {
        return tip;
    }

    public void setTip(Double tip) {
        this.tip = tip;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Integer getSplitCount() {
        return splitCount;
    }

    public void setSplitCount(Integer splitCount) {
        this.splitCount = splitCount;
    }

    public List<PartPaymentEntry> getPartPayments() {
        return partPayments;
    }

    public void setPartPayments(List<PartPaymentEntry> partPayments) {
        this.partPayments = partPayments;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
}