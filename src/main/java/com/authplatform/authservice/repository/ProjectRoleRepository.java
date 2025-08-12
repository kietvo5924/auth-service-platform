package com.authplatform.authservice.repository;

import com.authplatform.authservice.model.Project;
import com.authplatform.authservice.model.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {
    Optional<ProjectRole> findByNameAndProject(String name, Project project);
    List<ProjectRole> findByProject(Project project);
    boolean existsByNameAndProject(String name, Project project);
    Optional<ProjectRole> findByIdAndProject(Long id, Project project);
}
