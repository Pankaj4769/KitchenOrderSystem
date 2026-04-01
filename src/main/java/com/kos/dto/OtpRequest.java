package com.kos.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpRequest {

    @NotBlank
    private String identifier;

    @NotBlank
    private String identifierType; // "username" | "email" | "mobile"

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getIdentifierType() { return identifierType; }
    public void setIdentifierType(String identifierType) { this.identifierType = identifierType; }
}
