package com.pehchaan.backend.controller;

import com.pehchaan.backend.dto.assignment.AssignmentRequest;
import com.pehchaan.backend.dto.assignment.AssignmentResponse;
import com.pehchaan.backend.dto.profile.ProfileResponse;
import com.pehchaan.backend.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping("/workers/search")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<List<ProfileResponse>> searchWorkers(@RequestParam String skill) {
        return ResponseEntity.ok(assignmentService.searchWorkers(skill));
    }

    @PostMapping
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<AssignmentResponse> createAssignment(@RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.createAssignment(request));
    }

    @GetMapping("/my-projects")
    @PreAuthorize("hasRole('LABOR')")
    public ResponseEntity<List<AssignmentResponse>> getMyAssignments() {
        return ResponseEntity.ok(assignmentService.getMyAssignments());
    }
}