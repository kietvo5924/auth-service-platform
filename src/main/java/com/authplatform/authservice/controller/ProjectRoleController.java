package com.authplatform.authservice.controller;

import com.authplatform.authservice.dto.ApiResponse;
import com.authplatform.authservice.dto.ProjectRoleRequest;
import com.authplatform.authservice.dto.ProjectRoleResponse;
import com.authplatform.authservice.service.ProjectRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/roles")
@RequiredArgsConstructor
public class ProjectRoleController {

    private final ProjectRoleService projectRoleService;

    @PostMapping
    @PreAuthorize("@permissionService.canManageProject(authentication, #projectId)")
    public ResponseEntity<ProjectRoleResponse> createRole(@PathVariable Long projectId, @Valid @RequestBody ProjectRoleRequest request) {
        ProjectRoleResponse newRole = projectRoleService.createRole(projectId, request);
        return new ResponseEntity<>(newRole, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("@permissionService.canManageProject(authentication, #projectId)")
    public ResponseEntity<List<ProjectRoleResponse>> getRoles(@PathVariable Long projectId) {
        List<ProjectRoleResponse> roles = projectRoleService.getRolesByProject(projectId);
        return ResponseEntity.ok(roles);
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("@permissionService.canManageProject(authentication, #projectId)")
    public ResponseEntity<ProjectRoleResponse> updateRole(@PathVariable Long projectId, @PathVariable Long roleId, @Valid @RequestBody ProjectRoleRequest request) {
        ProjectRoleResponse updatedRole = projectRoleService.updateRole(projectId, roleId, request);
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("@permissionService.canManageProject(authentication, #projectId)")
    public ResponseEntity<ApiResponse> deleteRole(@PathVariable Long projectId, @PathVariable Long roleId) {
        projectRoleService.deleteRole(projectId, roleId);
        return ResponseEntity.ok(new ApiResponse(true, "Role deleted successfully."));
    }
}
