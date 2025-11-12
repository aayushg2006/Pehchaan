package com.pehchaan.backend.service;

import com.pehchaan.backend.dto.project.CreateProjectRequest;
import com.pehchaan.backend.dto.project.ProjectResponse;
import com.pehchaan.backend.entity.Project;
import com.pehchaan.backend.entity.User;
import com.pehchaan.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pehchaan.backend.repository.UserRepository; // Import UserRepository
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository; // Need this to find the contractor
    private final GeometryFactory geometryFactory; // Our new bean

    /**
     * Creates a new project for the currently logged-in contractor.
     */
    public ProjectResponse createProject(CreateProjectRequest request) {
        // 1. Get the authenticated contractor
        User contractor = getAuthenticatedUser();

        // 2. Create a PostGIS Point from the latitude/longitude
        Coordinate coordinate = new Coordinate(request.getLongitude(), request.getLatitude());
        Point locationPoint = geometryFactory.createPoint(coordinate);

        // 3. Build the new Project entity
        Project project = Project.builder()
                .name(request.getName())
                .address(request.getAddress())
                .location(locationPoint) // Set the geo-point
                .contractor(contractor)
                .wageRate(request.getWageRate())
                .wageType(request.getWageType())
                .build();

        // 4. Save to the database
        Project savedProject = projectRepository.save(project);

        // 5. Return the "safe" DTO response
        return ProjectResponse.fromEntity(savedProject);
    }

    /**
     * Gets all projects for the currently logged-in contractor.
     */
    public List<ProjectResponse> getMyProjects() {
        User contractor = getAuthenticatedUser();
        
        return projectRepository.findByContractorId(contractor.getId())
                .stream()
                .map(ProjectResponse::fromEntity) // Convert each Project to a ProjectResponse
                .collect(Collectors.toList());
    }

    // --- Private Helper Method ---

    private User getAuthenticatedUser() {
        String userPhone = SecurityContextHolder.getContext()
                                                .getAuthentication()
                                                .getName();
        
        return userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userPhone));
    }
}