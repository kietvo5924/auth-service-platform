package com.authplatform.authservice.dto;

import com.authplatform.authservice.model.Role;
import lombok.Data;

@Data
public class OwnerResponse {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private boolean emailVerified;
    private boolean locked;
}
