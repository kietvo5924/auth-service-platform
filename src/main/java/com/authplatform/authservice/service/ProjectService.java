package com.authplatform.authservice.service;

import com.authplatform.authservice.dto.CreateProjectRequest;
import com.authplatform.authservice.dto.ProjectResponse;
import com.authplatform.authservice.dto.UpdateProjectRequest;
import com.authplatform.authservice.exception.ProjectNotFoundException;
import com.authplatform.authservice.model.Owner;
import com.authplatform.authservice.model.Project;
import com.authplatform.authservice.model.ProjectRole;
import com.authplatform.authservice.repository.OwnerRepository;
import com.authplatform.authservice.repository.ProjectRepository;
import com.authplatform.authservice.repository.ProjectRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OwnerRepository ownerRepository;
    private final ProjectRoleRepository projectRoleRepository;

    public Long getProjectIdByApiKey(String apiKey) {
        Project project = projectRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with the given API key."));
        return project.getId();
    }

    @Transactional
    public ProjectResponse createProject(String ownerEmail, CreateProjectRequest request) {
        Owner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found"));

        Project newProject = Project.builder()
                .name(request.getName())
                .owner(owner)
                .apiKey(UUID.randomUUID().toString())
                .allowedOrigins(request.getAllowedOrigins() != null ? request.getAllowedOrigins() : new ArrayList<>())
                .build();

        Project savedProject = projectRepository.save(newProject);

        ProjectRole adminRole = ProjectRole.builder().name("ADMIN").project(savedProject).level(1000).build();
        ProjectRole userRole = ProjectRole.builder().name("USER").project(savedProject).level(10).build();
        projectRoleRepository.saveAll(Arrays.asList(userRole, adminRole));

        return mapToProjectResponse(newProject);
    }

    @Transactional
    public ProjectResponse updateProject(String ownerEmail, Long projectId, UpdateProjectRequest request) {
        Owner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You do not have permission to update this project.");
        }

        project.setName(request.getName());
        project.setAllowedOrigins(request.getAllowedOrigins() != null ? request.getAllowedOrigins() : new ArrayList<>());
        Project updatedProject = projectRepository.save(project);
        return mapToProjectResponse(updatedProject);
    }

    public List<ProjectResponse> getProjectsByOwner(String ownerEmail) {
        Owner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found"));

        return projectRepository.findByOwner(owner).stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(String ownerEmail, Long projectId) {
        Owner owner = ownerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        if (!project.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You do not have permission to access this project.");
        }

        return mapToProjectResponse(project);
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setApiKey(project.getApiKey());
        response.setAllowedOrigins(project.getAllowedOrigins());
        return response;
    }

}
