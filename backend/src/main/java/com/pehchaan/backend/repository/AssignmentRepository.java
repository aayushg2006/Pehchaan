package com.pehchaan.backend.repository;

import com.pehchaan.backend.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
    // Finds all assignments for a specific laborer
    List<Assignment> findByLaborerId(Long laborerId);
    
    // Finds a specific assignment for a laborer and project
    Optional<Assignment> findByLaborerIdAndProjectId(Long laborerId, Long projectId);
}