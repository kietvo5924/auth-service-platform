package com.authplatform.authservice.controller;

import com.authplatform.authservice.dto.OwnerResponse;
import com.authplatform.authservice.dto.UpdateOwnerRequest;
import com.authplatform.authservice.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OwnerResponse> updateMyProfile(@Valid @RequestBody UpdateOwnerRequest request, Principal principal) {
        OwnerResponse updateOwner = ownerService.updateOwner(principal.getName(), request);
        return ResponseEntity.ok(updateOwner);
    }

}
