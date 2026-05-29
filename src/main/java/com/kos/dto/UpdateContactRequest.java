package com.kos.dto;

public class UpdateContactRequest {
    private String field;     // "mobile" or "email"
    private String newValue;

    public UpdateContactRequest() {}

    public String getField() { return field; }
    public void setField(String v) { this.field = v; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String v) { this.newValue = v; }
}
