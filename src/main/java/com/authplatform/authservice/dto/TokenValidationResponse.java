package com.authplatform.authservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class TokenValidationResponse {
    private boolean valid;
    private String email;
    private Long userId;
    private Set<String> roles;
}
