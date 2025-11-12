package com.pehchaan.backend.controller;

import com.pehchaan.backend.dto.profile.ProfileResponse;
import com.pehchaan.backend.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    /**
     * GET /api/workers/nearby?skill=...&lat=...&lon=...
     * This is the "Find a Plumber" endpoint for the Consumer Portal.
     */
    @GetMapping("/nearby")
    @PreAuthorize("hasRole('CONSUMER')")
    public ResponseEntity<List<ProfileResponse>> findNearbyWorkers(
            @RequestParam String skill,
            @RequestParam double lat,
            @RequestParam double lon
    ) {
        List<ProfileResponse> workers = workerService.findNearbyWorkers(skill, lat, lon);
        return ResponseEntity.ok(workers);
    }
}