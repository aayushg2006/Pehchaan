package com.pehchaan.backend.service;

import com.pehchaan.backend.dto.project.CreateProjectRequest;
import com.pehchaan.backend.dto.project.ProjectResponse;
import com.pehchaan.backend.entity.Project;
import com.pehchaan.backend.entity.User;
import com.pehchaan.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory; // ✅ IMPORT
import org.locationtech.jts.geom.Point;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.pehchaan.backend.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory; // ✅ ADD THIS INJECTION

    public ProjectResponse createProject(CreateProjectRequest request) {
        User contractor = getAuthenticatedUser();

        // This was causing a NullPointerException
        Coordinate coordinate = new Coordinate(request.getLongitude(), request.getLatitude());
        Point locationPoint = geometryFactory.createPoint(coordinate);

        Project project = Project.builder()
                .name(request.getName())
                .address(request.getAddress())
                .location(locationPoint) 
                .contractor(contractor)
                .build();

        Project savedProject = projectRepository.save(project);
        return ProjectResponse.fromEntity(savedProject);
    }

    public List<ProjectResponse> getMyProjects() {
        User contractor = getAuthenticatedUser();
        return projectRepository.findByContractorId(contractor.getId())
                .stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        String userPhone = SecurityContextHolder.getContext()
                                                .getAuthentication()
                                                .getName();
        return userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userPhone));
    }
}