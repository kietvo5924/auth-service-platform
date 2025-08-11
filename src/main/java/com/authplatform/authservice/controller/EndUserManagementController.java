package com.authplatform.authservice.controller;

import com.authplatform.authservice.dto.ApiResponse;
import com.authplatform.authservice.dto.EndUserResponse;
import com.authplatform.authservice.dto.UpdateEndUserRequest;
import com.authplatform.authservice.dto.UpdateEndUserRolesRequest;
import com.authplatform.authservice.service.EndUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/endusers")
public class EndUserManagementController {

    private final EndUserService endUserService;

    // Sửa thông tin fullName
    @PutMapping("/{endUserId}")
    @PreAuthorize("@permissionService.canManageProject(authentication, #projectId)")
    public ResponseEntity<EndUserResponse> updateUserDetails(
            @PathVariable Long projectId,
            @PathVariable Long endUserId,
            @Valid @RequestBody UpdateEndUserRequest request
    ) {
        EndUserResponse updatedUser = endUserService.updateUserDetails(projectId, endUserId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{endUserId}/roles")
    @PreAuthorize("@permissionService.canManageProject(authentication, #projectId)")
    public ResponseEntity<EndUserResponse> updateUserRoles(
            @PathVariable Long projectId,
            @PathVariable Long endUserId,
            @Valid @RequestBody UpdateEndUserRolesRequest request
    ) {
        EndUserResponse updatedUser = endUserService.updateUserRoles(projectId, endUserId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{endUserId}/roles")
    @PreAuthorize("@permissionService.canManageProject(authentication, #projectId)")
    public ResponseEntity<EndUserResponse> addRolesToUser(
            @PathVariable Long projectId,
            @PathVariable Long endUserId,
            @Valid @RequestBody UpdateEndUserRolesRequest request
    ) {
        EndUserResponse updatedUser = endUserService.addRolesToUser(projectId, endUserId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{endUserId}")
    @PreAuthorize("@permissionService.canManageProject(authentication, #projectId)")
    public ResponseEntity<EndUserResponse> getEndUserById(
            @PathVariable Long projectId,
            @PathVariable Long endUserId
    ) {
        EndUserResponse endUser = endUserService.getEndUserById(projectId, endUserId);
        return ResponseEntity.ok(endUser);
    }

    // Khóa tài khoản
    @PostMapping("/{endUserId}/lock")
    @PreAuthorize("@permissionService.canManageProject(authentication, #projectId)")
    public ResponseEntity<ApiResponse> lockUser(
            @PathVariable Long projectId,
            @PathVariable Long endUserId
    ) {
        endUserService.updateUserLockStatus(projectId, endUserId, true);
        return ResponseEntity.ok(new ApiResponse(true, "User has been locked."));
    }

    // Mở khóa tài khoản
    @PostMapping("/{endUserId}/unlock")
    @PreAuthorize("@permissionService.canManageProject(authentication, #projectId)")
    public ResponseEntity<ApiResponse> unlockUser(
            @PathVariable Long projectId,
            @PathVariable Long endUserId,
            Principal principal
    ) {
        endUserService.updateUserLockStatus(projectId, endUserId, false);
        return ResponseEntity.ok(new ApiResponse(true, "User has been unlocked."));
    }

    @DeleteMapping("/{endUserId}/roles/{roleId}")
    @PreAuthorize("@permissionService.canManageProject(authentication, #projectId)")
    public ResponseEntity<ApiResponse> removeRoleFromUser(
            @PathVariable Long projectId,
            @PathVariable Long endUserId,
            @PathVariable Long roleId,
            Principal principal
    ) {
        endUserService.removeRoleFromUser(projectId, endUserId, roleId);
        return ResponseEntity.ok(new ApiResponse(true, "Role removed from user successfully."));
    }
}
