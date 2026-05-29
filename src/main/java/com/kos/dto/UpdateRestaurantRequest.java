package com.kos.dto;

import java.math.BigDecimal;

public class UpdateRestaurantRequest {
    private String address;
    private String gstin;
    private String fssai;

    // Billing & Receipt
    private String     taxName;
    private BigDecimal taxRate;
    private String     taxInclusion;
    private BigDecimal serviceCharge;
    private String     receiptHeader;
    private String     receiptFooter;
    private Boolean    autoPrintReceipt;
    private Boolean    showGstinOnReceipt;

    public UpdateRestaurantRequest() {}

    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public String getGstin() { return gstin; }
    public void setGstin(String v) { this.gstin = v; }
    public String getFssai() { return fssai; }
    public void setFssai(String v) { this.fssai = v; }

    public String getTaxName() { return taxName; }
    public void setTaxName(String v) { this.taxName = v; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal v) { this.taxRate = v; }
    public String getTaxInclusion() { return taxInclusion; }
    public void setTaxInclusion(String v) { this.taxInclusion = v; }
    public BigDecimal getServiceCharge() { return serviceCharge; }
    public void setServiceCharge(BigDecimal v) { this.serviceCharge = v; }
    public String getReceiptHeader() { return receiptHeader; }
    public void setReceiptHeader(String v) { this.receiptHeader = v; }
    public String getReceiptFooter() { return receiptFooter; }
    public void setReceiptFooter(String v) { this.receiptFooter = v; }
    public Boolean getAutoPrintReceipt() { return autoPrintReceipt; }
    public void setAutoPrintReceipt(Boolean v) { this.autoPrintReceipt = v; }
    public Boolean getShowGstinOnReceipt() { return showGstinOnReceipt; }
    public void setShowGstinOnReceipt(Boolean v) { this.showGstinOnReceipt = v; }
}
