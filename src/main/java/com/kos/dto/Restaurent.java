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

    // ── Billing & Receipt ──
    @Column(length = 20)
    private String taxName;

    private java.math.BigDecimal taxRate;

    @Column(length = 10)
    private String taxInclusion;             // "exclusive" | "inclusive"

    private java.math.BigDecimal serviceCharge;

    @Column(length = 500)
    private String receiptHeader;

    @Column(length = 500)
    private String receiptFooter;

    private Boolean autoPrintReceipt;
    private Boolean showGstinOnReceipt;

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

    public String getTaxName() { return taxName; }
    public void setTaxName(String taxName) { this.taxName = taxName; }

    public java.math.BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(java.math.BigDecimal taxRate) { this.taxRate = taxRate; }

    public String getTaxInclusion() { return taxInclusion; }
    public void setTaxInclusion(String taxInclusion) { this.taxInclusion = taxInclusion; }

    public java.math.BigDecimal getServiceCharge() { return serviceCharge; }
    public void setServiceCharge(java.math.BigDecimal serviceCharge) { this.serviceCharge = serviceCharge; }

    public String getReceiptHeader() { return receiptHeader; }
    public void setReceiptHeader(String receiptHeader) { this.receiptHeader = receiptHeader; }

    public String getReceiptFooter() { return receiptFooter; }
    public void setReceiptFooter(String receiptFooter) { this.receiptFooter = receiptFooter; }

    public Boolean getAutoPrintReceipt() { return autoPrintReceipt; }
    public void setAutoPrintReceipt(Boolean autoPrintReceipt) { this.autoPrintReceipt = autoPrintReceipt; }

    public Boolean getShowGstinOnReceipt() { return showGstinOnReceipt; }
    public void setShowGstinOnReceipt(Boolean showGstinOnReceipt) { this.showGstinOnReceipt = showGstinOnReceipt; }
}
