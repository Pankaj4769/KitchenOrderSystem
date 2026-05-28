package com.kos.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Restaurent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer restaurentId;

    private String restaurentName;

    @Column(length = 500)
    private String address;

    @Column(length = 15)
    private String gstin;

    @Column(length = 14)
    private String fssai;

    public Integer getRestaurentId() { return restaurentId; }
    public void setRestaurentId(Integer restaurentId) { this.restaurentId = restaurentId; }

    public String getRestaurentName() { return restaurentName; }
    public void setRestaurentName(String restaurentName) { this.restaurentName = restaurentName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getFssai() { return fssai; }
    public void setFssai(String fssai) { this.fssai = fssai; }
}
