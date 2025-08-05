package com.authplatform.authservice.controller;

import com.authplatform.authservice.dto.ApiResponse;
import com.authplatform.authservice.dto.OwnerResponse;
import com.authplatform.authservice.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
