package com.authplatform.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EndUserLoginRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
