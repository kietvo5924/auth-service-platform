package com.authplatform.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;

    @NotBlank
    @Size(min = 8, message = "New password must be at least 8 characters")
    private String newPassword;
}
