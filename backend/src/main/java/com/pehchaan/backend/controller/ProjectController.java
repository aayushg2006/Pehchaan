package com.pehchaan.backend.controller;

import com.pehchaan.backend.dto.project.CreateProjectRequest;
import com.pehchaan.backend.dto.project.ProjectResponse;
import com.pehchaan.backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * POST /api/projects
     * Creates a new project. This endpoint is restricted to Contractors only.
     * The CreateProjectRequest DTO no longer contains wage info.
     */
    @PostMapping
    @PreAuthorize("hasRole('CONTRACTOR')") // Only users with ROLE_CONTRACTOR can access
    public ResponseEntity<ProjectResponse> createProject(
            @RequestBody CreateProjectRequest request
    ) {
        ProjectResponse project = projectService.createProject(request);
        return ResponseEntity.ok(project);
    }

    /**
     * GET /api/projects/my-projects
     * Gets all projects for the currently logged-in contractor.
     */
    @GetMapping("/my-projects")
    @PreAuthorize("hasRole('CONTRACTOR')") // Only users with ROLE_CONTRACTOR can access
    public ResponseEntity<List<ProjectResponse>> getMyProjects() {
        List<ProjectResponse> projects = projectService.getMyProjects();
        return ResponseEntity.ok(projects);
    }

    // ❌ REMOVED: The GET /api/projects endpoint is deleted.
    // Laborers now use GET /api/assignments/my-projects to find work.
}