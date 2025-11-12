package com.pehchaan.backend.service;

import com.pehchaan.backend.dto.assignment.AssignmentRequest;
import com.pehchaan.backend.dto.assignment.AssignmentResponse;
import com.pehchaan.backend.dto.profile.ProfileResponse;
import com.pehchaan.backend.entity.Assignment;
import com.pehchaan.backend.entity.Project;
import com.pehchaan.backend.entity.User;
import com.pehchaan.backend.repository.AssignmentRepository;
import com.pehchaan.backend.repository.ProjectRepository;
import com.pehchaan.backend.repository.UserRepository;
import com.pehchaan.backend.repository.WorkerSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkerSearchRepository workerSearchRepository;
    // ❌ REMOVED: This unused dependency was causing the error.
    // private final ProfileService profileService;

    /**
     * Search for available workers by skill.
     */
    public List<ProfileResponse> searchWorkers(String skill) {
        return workerSearchRepository.searchWorkersBySkill(skill)
                .stream()
                .map(ProfileResponse::fromEntity) // Use the DTO helper
                .collect(Collectors.toList());
    }

    /**
     * Assign a worker to a project (Contractor Only).
     */
    @Transactional
    public AssignmentResponse createAssignment(AssignmentRequest request) {
        User contractor = getAuthenticatedUser();
        
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        
        User laborer = userRepository.findById(request.getLaborerId())
                .orElseThrow(() -> new IllegalArgumentException("Laborer not found"));

        // Security Check: Ensure the contractor owns this project
        if (!project.getContractor().getId().equals(contractor.getId())) {
            throw new AccessDeniedException("You do not own this project.");
        }

        // Check if assignment already exists
        assignmentRepository.findByLaborerIdAndProjectId(laborer.getId(), project.getId())
                .ifPresent(a -> {
                    throw new IllegalStateException("This worker is already assigned to this project.");
                });

        Assignment newAssignment = Assignment.builder()
                .project(project)
                .laborer(laborer)
                .wageRate(request.getWageRate())
                .wageType(request.getWageType())
                .build();
        
        Assignment savedAssignment = assignmentRepository.save(newAssignment);
        return AssignmentResponse.fromEntity(savedAssignment);
    }

    /**
     * Get all assignments for the currently logged-in laborer.
     */
    public List<AssignmentResponse> getMyAssignments() {
        User laborer = getAuthenticatedUser();
        return assignmentRepository.findByLaborerId(laborer.getId())
                .stream()
                .map(AssignmentResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    private User getAuthenticatedUser() {
        String userPhone = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userPhone));
    }
}