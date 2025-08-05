package com.authplatform.authservice.service;

import com.authplatform.authservice.dto.ProjectRoleRequest;
import com.authplatform.authservice.dto.ProjectRoleResponse;
import com.authplatform.authservice.exception.ProjectNotFoundException;
import com.authplatform.authservice.model.Owner;
import com.authplatform.authservice.model.Project;
import com.authplatform.authservice.model.ProjectRole;
import com.authplatform.authservice.repository.EndUserRepository;
import com.authplatform.authservice.repository.OwnerRepository;
import com.authplatform.authservice.repository.ProjectRepository;
import com.authplatform.authservice.repository.ProjectRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectRoleService {

    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectRepository projectRepository;
    private final EndUserRepository endUserRepository;

    // Tạo role
    @Transactional
    public ProjectRoleResponse createRole(Long projectId, ProjectRoleRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        String normalizedName = normalizeRoleName(request.getName());

        if (projectRoleRepository.existsByNameAndProject(request.getName(), project)) {
            throw new IllegalStateException("Role with name '" + request.getName() + "' already exists in this project.");
        }

        ProjectRole newRole = ProjectRole.builder()
                .name(normalizedName)
                .level(request.getLevel())
                .project(project)
                .build();

        ProjectRole savedRole = projectRoleRepository.save(newRole);
        return mapToResponse(savedRole);
    }

    // Lấy tất cả role
    public List<ProjectRoleResponse> getRolesByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
        return projectRoleRepository.findByProject(project).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Cập nhật role
    @Transactional
    public ProjectRoleResponse updateRole(Long projectId, Long roleId, ProjectRoleRequest request) {
        ProjectRole role = findRoleAndVerifyProject(roleId, projectId);

        String normalizedName = normalizeRoleName(request.getName());

        if (!role.getName().equals(normalizedName) && projectRoleRepository.existsByNameAndProject(normalizedName, role.getProject())) {
            throw new IllegalStateException("Role with name '" + normalizedName + "' already exists in this project.");
        }

        role.setName(normalizedName);
        role.setLevel(request.getLevel());

        ProjectRole savedRole = projectRoleRepository.save(role);
        return mapToResponse(savedRole);
    }

    // Xóa role
    @Transactional
    public void deleteRole(Long projectId, Long roleId) {
        ProjectRole role = findRoleAndVerifyProject(roleId, projectId);

        if (role.getName().equals("USER") || role.getName().equals("ADMIN")) {
            throw new IllegalStateException("Cannot delete default roles.");
        }

        long userCount = endUserRepository.countByRolesContaining(role);
        if (userCount > 0) {
            throw new IllegalStateException("Cannot delete role '" + role.getName() + "' because it is currently assigned to " + userCount + " user(s).");
        }

        projectRoleRepository.delete(role);
    }

    // --- CÁC PHƯƠNG THỨC HỖ TRỢ ---

    private ProjectRole findRoleAndVerifyProject(Long roleId, Long projectId) {
        ProjectRole role = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalStateException("Role not found with id: " + roleId));
        if (!role.getProject().getId().equals(projectId)) {
            throw new AccessDeniedException("This role does not belong to the specified project.");
        }
        return role;
    }

    private String normalizeRoleName(String name) {
        // Loại bỏ dấu Tiếng Việt
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{M}");
        normalized = pattern.matcher(normalized).replaceAll("");

        // Thay thế khoảng trắng bằng gạch dưới và chuyền thành IN HOA
        return normalized.trim().replaceAll("\\s+","_").toUpperCase();
    }

    private ProjectRoleResponse mapToResponse(ProjectRole role) {
        ProjectRoleResponse res = new ProjectRoleResponse();
        res.setId(role.getId());
        res.setName(role.getName());
        res.setLevel(role.getLevel());
        return res;
    }

}
