package com.authplatform.authservice.dto;

import lombok.Data;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String apiKey;
}
