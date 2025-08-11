package com.authplatform.authservice.controller;

import com.authplatform.authservice.dto.ApiResponse;
import com.authplatform.authservice.dto.ChangePasswordRequest;
import com.authplatform.authservice.dto.OwnerResponse;
import com.authplatform.authservice.dto.UpdateOwnerRequest;
import com.authplatform.authservice.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OwnerResponse> getMyProfile(Principal principal) {
        OwnerResponse profile = ownerService.getOwnerProfile(principal.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> changeMyPassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal
    ) {
        ownerService.changeOwnerPassword(principal.getName(), request);
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully."));
    }
}
