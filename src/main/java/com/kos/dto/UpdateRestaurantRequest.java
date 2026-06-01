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

    // POS
    private Boolean kotEnabled;
    private Boolean kotAutoPrint;
    private String  kotPrinter;
    private Boolean orderSoundAlert;
    private Integer totalTables;
    private Boolean tableReservations;
    private Boolean multiKotRounds;
    private Boolean deliveryEnabled;
    private Boolean takeawayEnabled;

    // Display
    private String  language;
    private String  currencyCode;
    private String  dateFormat;
    private String  timeFormat;
    private Boolean notifyLowStock;
    private Integer lowStockThreshold;
    private Boolean notifyNewOrder;
    private Boolean browserNotifications;

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
    public Boolean getKotEnabled() { return kotEnabled; }
    public void setKotEnabled(Boolean v) { this.kotEnabled = v; }
    public Boolean getKotAutoPrint() { return kotAutoPrint; }
    public void setKotAutoPrint(Boolean v) { this.kotAutoPrint = v; }
    public String getKotPrinter() { return kotPrinter; }
    public void setKotPrinter(String v) { this.kotPrinter = v; }
    public Boolean getOrderSoundAlert() { return orderSoundAlert; }
    public void setOrderSoundAlert(Boolean v) { this.orderSoundAlert = v; }
    public Integer getTotalTables() { return totalTables; }
    public void setTotalTables(Integer v) { this.totalTables = v; }
    public Boolean getTableReservations() { return tableReservations; }
    public void setTableReservations(Boolean v) { this.tableReservations = v; }
    public Boolean getMultiKotRounds() { return multiKotRounds; }
    public void setMultiKotRounds(Boolean v) { this.multiKotRounds = v; }
    public Boolean getDeliveryEnabled() { return deliveryEnabled; }
    public void setDeliveryEnabled(Boolean v) { this.deliveryEnabled = v; }
    public Boolean getTakeawayEnabled() { return takeawayEnabled; }
    public void setTakeawayEnabled(Boolean v) { this.takeawayEnabled = v; }
    public String getLanguage() { return language; }
    public void setLanguage(String v) { this.language = v; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String v) { this.currencyCode = v; }
    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String v) { this.dateFormat = v; }
    public String getTimeFormat() { return timeFormat; }
    public void setTimeFormat(String v) { this.timeFormat = v; }
    public Boolean getNotifyLowStock() { return notifyLowStock; }
    public void setNotifyLowStock(Boolean v) { this.notifyLowStock = v; }
    public Integer getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(Integer v) { this.lowStockThreshold = v; }
    public Boolean getNotifyNewOrder() { return notifyNewOrder; }
    public void setNotifyNewOrder(Boolean v) { this.notifyNewOrder = v; }
    public Boolean getBrowserNotifications() { return browserNotifications; }
    public void setBrowserNotifications(Boolean v) { this.browserNotifications = v; }
}
