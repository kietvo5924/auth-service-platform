package com.authplatform.authservice.controller;

import com.authplatform.authservice.dto.ApiResponse;
import com.authplatform.authservice.dto.OwnerResponse;
import com.authplatform.authservice.dto.UpdateOwnerRoleRequest;
import com.authplatform.authservice.model.Owner;
import com.authplatform.authservice.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final OwnerService ownerService;

    @GetMapping("/owners")
    public ResponseEntity<List<OwnerResponse>> getAllOwners() {
        return ResponseEntity.ok(ownerService.getAllOwners());
    }

    @PutMapping("/owners/{id}/role")
    public ResponseEntity<ApiResponse> updateOwnerRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOwnerRoleRequest request,
            Authentication authentication // Lấy thông tin admin đang đăng nhập
    ) {
        Owner currentAdmin = (Owner) authentication.getPrincipal();
        ownerService.updateOwnerRole(currentAdmin.getId(), id, request);
        return ResponseEntity.ok(new ApiResponse(true, "Owner role updated successfully."));
    }

    @PostMapping("/owners/{id}/lock")
    public ResponseEntity<ApiResponse> lockOwner(@PathVariable Long id) {
        ownerService.toggleOwnerLock(id, true);
        return ResponseEntity.ok(new ApiResponse(true, "Owner locked successfully."));
    }

    @PostMapping("/owners/{id}/unlock")
    public ResponseEntity<ApiResponse> unlockOwner(@PathVariable Long id) {
        ownerService.toggleOwnerLock(id, false);
        return ResponseEntity.ok(new ApiResponse(true, "Owner unlocked successfully."));
    }

}
