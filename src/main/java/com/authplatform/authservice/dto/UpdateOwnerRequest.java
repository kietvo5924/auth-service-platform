package com.authplatform.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateOwnerRequest {
    @NotBlank(message = "Full name cannot be blank")
    @Size(max = 100)
    private String fullName;
}
