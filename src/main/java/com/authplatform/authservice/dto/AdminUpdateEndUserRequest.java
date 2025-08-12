package com.authplatform.authservice.dto;

import lombok.Data;

import java.util.Set;

@Data
public class AdminUpdateEndUserRequest {
    private String fullName;
    private Set<Long> roleIds;
}