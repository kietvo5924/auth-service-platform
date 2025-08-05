package com.authplatform.authservice.service;

import com.authplatform.authservice.model.EndUser;
import com.authplatform.authservice.model.Owner;
import com.authplatform.authservice.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("permissionService")
@RequiredArgsConstructor
public class PermissionService {

    private final ProjectRepository projectRepository;

    public boolean canManageProject(Authentication authentication, Long projectId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        // Trường hợp 1: Người dùng là Owner
        if (principal instanceof Owner owner) {
            // Kiểm tra xem project có thuộc sở hữu của owner này không
            return projectRepository.findById(projectId)
                    .map(project -> project.getOwner().getId().equals(owner.getId()))
                    .orElse(false);
        }

        // Trường hợp 2: Người dùng là EndUser
        if (principal instanceof EndUser endUser) {
            // 1. Kiểm tra xem user có thuộc project này không
            boolean isUserInProject = endUser.getProject().getId().equals(projectId);
            if (!isUserInProject) {
                return false;
            }

            // 2. Kiểm tra xem user có vai trò ADMIN không
            return endUser.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        }

        return false;
    }

}
