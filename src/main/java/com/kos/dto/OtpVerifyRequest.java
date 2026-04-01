package com.kos.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpVerifyRequest {

    @NotBlank
    private String identifier;

    @NotBlank
    private String identifierType; // "username" | "email" | "mobile"

    @NotBlank
    private String otp;

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getIdentifierType() { return identifierType; }
    public void setIdentifierType(String identifierType) { this.identifierType = identifierType; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
