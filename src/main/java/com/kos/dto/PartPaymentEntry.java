package com.kos.dto;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "part_payment_entry")
public class PartPaymentEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fixed: 'index' is a reserved keyword
    @Column(name = "entry_index", nullable = false)
    private int entryIndex;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    private String note;

    // Fixed: 'timestamp' is a reserved keyword
    @Column(name = "created_at")
    private Instant createdAt;

    // Constructors
    public PartPaymentEntry() {
    }

    public PartPaymentEntry(int entryIndex, double amount, PaymentMethod method, Instant createdAt) {
        this.entryIndex = entryIndex;
        this.amount = amount;
        this.method = method;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public int getEntryIndex() {
        return entryIndex;
    }

    public void setEntryIndex(int entryIndex) {
        this.entryIndex = entryIndex;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}