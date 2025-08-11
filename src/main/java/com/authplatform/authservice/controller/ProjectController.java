package com.authplatform.authservice.controller;

import com.authplatform.authservice.dto.CreateProjectRequest;
import com.authplatform.authservice.dto.EndUserResponse;
import com.authplatform.authservice.dto.ProjectResponse;
import com.authplatform.authservice.dto.UpdateProjectRequest;
import com.authplatform.authservice.service.EndUserService;
import com.authplatform.authservice.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final EndUserService endUserService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request, Principal principal) {
        ProjectResponse newProject = projectService.createProject(principal.getName(), request);
        return new ResponseEntity<>(newProject, HttpStatus.CREATED);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long projectId, @Valid @RequestBody UpdateProjectRequest request, Principal principal) {
        ProjectResponse updatedProject = projectService.updateProject(principal.getName(), projectId, request);
        return ResponseEntity.ok(updatedProject);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(Principal principal) {
        List<ProjectResponse> projects = projectService.getProjectsByOwner(principal.getName());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long projectId,
            Principal principal
    ) {
        ProjectResponse project = projectService.getProjectById(principal.getName(), projectId);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/{projectId}/endusers")
    @PreAuthorize("@permissionService.canManageProject(authentication, #projectId)")
    public ResponseEntity<List<EndUserResponse>> getEndUsersByProject(
            @PathVariable Long projectId
    ) {
        List<EndUserResponse> endUsers = endUserService.getUsersByProject(projectId);
        return ResponseEntity.ok(endUsers);
    }
}
