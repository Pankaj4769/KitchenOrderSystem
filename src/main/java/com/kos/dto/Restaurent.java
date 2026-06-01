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

    // ── POS ──
    private Boolean kotEnabled;
    private Boolean kotAutoPrint;

    @Column(length = 60)
    private String kotPrinter;

    private Boolean orderSoundAlert;
    private Integer totalTables;
    private Boolean tableReservations;
    private Boolean multiKotRounds;
    private Boolean deliveryEnabled;
    private Boolean takeawayEnabled;

    // ── Display ──
    @Column(length = 5)
    private String language;

    @Column(name = "currency_code", length = 5)
    private String currencyCode;

    @Column(length = 15)
    private String dateFormat;

    @Column(length = 5)
    private String timeFormat;

    private Boolean notifyLowStock;
    private Integer lowStockThreshold;
    private Boolean notifyNewOrder;
    private Boolean browserNotifications;

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
