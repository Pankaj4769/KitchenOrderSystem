package com.kos.dto;

import jakarta.validation.constraints.NotBlank;

public class ResetTempPasswordRequest {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "newPassword is required")
    private String newPassword;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
