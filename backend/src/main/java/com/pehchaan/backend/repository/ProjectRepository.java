package com.pehchaan.backend.repository;

import java.util.List;
import org.locationtech.jts.geom.Point; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; 
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.pehchaan.backend.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByContractorId(Long contractorId);

    /**
     * ✅ FIXED: Using named parameters instead of positional parameters
     * This avoids the parameter parsing issue with PostgreSQL type casting
     */
    @Query(value = "SELECT ST_DWithin(CAST(p.location AS geography), CAST(:laborerLocation AS geography), 200) " +
                   "FROM projects p WHERE p.id = :projectId",
           nativeQuery = true)
    boolean isLaborerOnSite(@Param("projectId") Long projectId, @Param("laborerLocation") Point laborerLocation);
}