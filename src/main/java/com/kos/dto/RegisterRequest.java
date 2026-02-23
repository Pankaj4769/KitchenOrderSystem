package com.kos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank
    private String username;

    @Email
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
