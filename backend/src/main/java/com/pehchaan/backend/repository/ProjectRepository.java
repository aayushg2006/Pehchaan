package com.pehchaan.backend.repository;

import java.util.List;
import org.locationtech.jts.geom.Point; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; 
import org.springframework.stereotype.Repository;
import com.pehchaan.backend.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByContractorId(Long contractorId);

    /**
     * ✅ FIXED: Added a space after ?2::geography
     * This fixes the "Ordinal parameter" parsing bug.
     */
    @Query(value = "SELECT ST_DWithin(p.location::geography, ?2::geography, 200) " +
                   "FROM projects p WHERE p.id = ?1",
           nativeQuery = true)
    boolean isLaborerOnSite(Long projectId, Point laborerLocation);
}