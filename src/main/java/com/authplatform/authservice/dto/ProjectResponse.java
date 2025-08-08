package com.authplatform.authservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String apiKey;
    private List<String> allowedOrigins;
}
