package com.pehchaan.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.pehchaan.backend.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Finds all projects created by a specific contractor.
     * Spring Data JPA automatically creates the query for this.
     */
    List<Project> findByContractorId(Long contractorId);
}