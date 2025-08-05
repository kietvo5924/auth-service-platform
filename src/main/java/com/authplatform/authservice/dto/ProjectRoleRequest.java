package com.authplatform.authservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectRoleRequest {
    @NotBlank
    @Size(max = 50)
    private String name;

    @Min(value = 1, message = "Level must be at least 1")
    private int level;
}
