package com.authplatform.authservice.dto;

import lombok.Data;

import java.util.Set;

@Data
public class EndUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private boolean emailVerified;
    private boolean locked;
    private Set<String> roles;
}
