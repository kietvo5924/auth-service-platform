package com.authplatform.authservice.repository;

import com.authplatform.authservice.model.Owner;
import com.authplatform.authservice.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // Tự động tìm kiếm tất cả các project thuộc về một Owner
    List<Project> findByOwner(Owner owner);

    Optional<Project> findByApiKey(String apiKey);
}
