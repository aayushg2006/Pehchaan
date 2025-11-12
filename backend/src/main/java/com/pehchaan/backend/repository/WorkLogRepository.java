package com.pehchaan.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.pehchaan.backend.entity.WorkLog;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {

    /**
     * Finds all work logs for a specific laborer.
     */
    List<WorkLog> findByLaborerId(Long laborerId);

    /**
     * Finds all work logs for a specific project.
     */
    List<WorkLog> findByProjectId(Long projectId);

    /**
     * This is a key method: It finds the "open" work log for a laborer.
     * (i.e., a log where they have checked IN but not checked OUT).
     * This is how we'll know if a user is currently on the clock.
     */
    Optional<WorkLog> findByLaborerIdAndCheckOutTimeIsNull(Long laborerId);

    /**
     * ✅ ADDED: Finds all work logs for a given list of project IDs.
     * This is for the contractor's dashboard.
     */
    List<WorkLog> findByProjectIdIn(List<Long> projectIds);
}