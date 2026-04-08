package com.kos.dto;

import jakarta.persistence.*;

@Entity
@Table(name = "addon")
public class Addon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // primary key

    private String name;

    private double price;

    // Constructors
    public Addon() {}

    public Addon(String name, double price) {
        this.name = name;
        this.price = price;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}