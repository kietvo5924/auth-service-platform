package com.authplatform.authservice.repository;

import com.authplatform.authservice.model.EndUser;
import com.authplatform.authservice.model.Project;
import com.authplatform.authservice.model.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EndUserRepository extends JpaRepository<EndUser, Long> {
    List<EndUser> findByProject(Project project);

    // Kiểm tra email có tồn tại trong một project cụ thể không
    boolean existsByEmailAndProject(String email, Project project);

    // Tìm EndUser bằng email trong một project cụ thể
    Optional<EndUser> findByEmailAndProject(String email, Project project);

    // Đếm số lượng EndUser có chứa một ProjectRole cụ thể
    long countByRolesContaining(ProjectRole projectRole);
}
