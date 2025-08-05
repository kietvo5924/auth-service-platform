package com.authplatform.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateEndUserRequest {
    @NotBlank
    private String fullName;
}
