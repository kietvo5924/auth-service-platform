package com.authplatform.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMyProfileRequest {
    @NotBlank(message = "Full name cannot be empty")
    @Size(max = 100)
    private String fullName;
}
