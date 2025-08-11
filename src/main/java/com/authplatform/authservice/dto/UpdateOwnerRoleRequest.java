package com.authplatform.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOwnerRoleRequest {
    @NotBlank
    private String role;
}
