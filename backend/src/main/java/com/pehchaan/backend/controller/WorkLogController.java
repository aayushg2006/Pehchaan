package com.pehchaan.backend.controller;

import com.pehchaan.backend.dto.work.CheckInRequest;
import com.pehchaan.backend.dto.work.WorkLogResponse;
import com.pehchaan.backend.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work")
@RequiredArgsConstructor
public class WorkLogController {

    private final WorkLogService workLogService;

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('LABOR')")
    public ResponseEntity<WorkLogResponse> checkIn(@RequestBody CheckInRequest request) {
        return ResponseEntity.ok(workLogService.checkIn(request));
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasRole('LABOR')")
    public ResponseEntity<WorkLogResponse> checkOut() {
        return ResponseEntity.ok(workLogService.checkOut());
    }

    @GetMapping("/my-logs")
    @PreAuthorize("hasAnyRole('LABOR', 'CONTRACTOR')")
    public ResponseEntity<List<WorkLogResponse>> getMyWorkLogs() { // ✅ Renamed method for consistency
        return ResponseEntity.ok(workLogService.getMyWorkLogs()); // ✅ FIXED: Changed to getMyWorkLogs()
    }

    /**
     * Endpoint for a contractor to approve a work log.
     * We use a path variable {id} to specify which log to approve.
     */
    @PostMapping("/logs/{id}/approve")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<WorkLogResponse> approveLog(@PathVariable("id") Long logId) {
        return ResponseEntity.ok(workLogService.approveWorkLog(logId));
    }
}