package com.authplatform.authservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateEndUserRolesRequest {
    @NotEmpty(message = "Role IDs cannot be empty")
    private Set<Long> roleIds;
}
