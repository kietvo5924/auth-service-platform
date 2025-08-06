package com.authplatform.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateProjectRequest {
    @NotBlank(message = "Project name cannot be blank")
    @Size(max = 100)
    private String name;

    private List<String> allowedOrigins;
}
