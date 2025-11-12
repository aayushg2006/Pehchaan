package com.pehchaan.backend.service;

import com.pehchaan.backend.dto.work.CheckInRequest;
import com.pehchaan.backend.dto.work.WorkLogResponse;
import com.pehchaan.backend.entity.Assignment; // ✅ ADD
import com.pehchaan.backend.entity.Project;
import com.pehchaan.backend.entity.User;
import com.pehchaan.backend.entity.WorkLog;
import com.pehchaan.backend.repository.AssignmentRepository; // ✅ ADD
import com.pehchaan.backend.repository.ProjectRepository;
import com.pehchaan.backend.repository.UserRepository;
import com.pehchaan.backend.repository.WorkLogRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.security.access.AccessDeniedException; 
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory;
    private final AssignmentRepository assignmentRepository; // ✅ ADD

    @Transactional
    public WorkLogResponse checkIn(@NonNull CheckInRequest request) {
        User laborer = getAuthenticatedUser();

        // 1. Check if laborer is already checked in
        workLogRepository.findByLaborerIdAndCheckOutTimeIsNull(laborer.getId())
            .ifPresent(activeLog -> {
                throw new IllegalStateException("User is already checked in to a project.");
            });

        // 2. ✅ Check if laborer is assigned to this project
        assignmentRepository.findByLaborerIdAndProjectId(laborer.getId(), request.getProjectId())
            .orElseThrow(() -> new AccessDeniedException("You are not assigned to this project."));

        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new IllegalArgumentException("Project not found."));
        
        Point laborerLocation = geometryFactory.createPoint(
            new Coordinate(request.getLongitude(), request.getLatitude())
        );

        // 3. Run the Geofence query (now checking meters)
        boolean onSite = projectRepository.isLaborerOnSite(project.getId(), laborerLocation);

        if (!onSite) {
            throw new IllegalStateException("You are not at the worksite. Check-in failed.");
        }

        // 4. Create and save the new "ACTIVE" work log
        WorkLog newLog = WorkLog.builder()
                .laborer(laborer)
                .project(project)
                .checkInTime(LocalDateTime.now())
                .status(WorkLog.WorkStatus.ACTIVE)
                .build();
        
        WorkLog savedLog = workLogRepository.save(newLog);
        return WorkLogResponse.fromEntity(savedLog);
    }

    @Transactional
    public WorkLogResponse checkOut() {
        User laborer = getAuthenticatedUser();

        WorkLog activeLog = workLogRepository.findByLaborerIdAndCheckOutTimeIsNull(laborer.getId())
            .orElseThrow(() -> new IllegalStateException("You are not checked in."));

        // ✅ REFACTORED: Get the Assignment to find the wage
        Assignment assignment = assignmentRepository.findByLaborerIdAndProjectId(
            laborer.getId(), 
            activeLog.getProject().getId()
        ).orElseThrow(() -> new IllegalStateException("No assignment found for this project."));

        LocalDateTime checkOutTime = LocalDateTime.now();

        // Calculate wage using the ASSIGNMENT'S wage details
        BigDecimal wageEarned = calculateWage(
            assignment.getWageType(), 
            assignment.getWageRate(), 
            activeLog.getCheckInTime(), 
            checkOutTime
        );

        activeLog.setCheckOutTime(checkOutTime);
        activeLog.setWageEarned(wageEarned);
        activeLog.setStatus(WorkLog.WorkStatus.PENDING_APPROVAL);
        WorkLog savedLog = workLogRepository.save(activeLog);
        return WorkLogResponse.fromEntity(savedLog);
    }

    @Transactional
    public WorkLogResponse approveWorkLog(Long logId) {
        // ... (This logic is unchanged)
        User contractor = getAuthenticatedUser();
        WorkLog log = workLogRepository.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("Work log not found."));
        if (!log.getProject().getContractor().getId().equals(contractor.getId())) {
            throw new AccessDeniedException("You are not authorized to approve this work log.");
        }
        if (log.getStatus() != WorkLog.WorkStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("This log is not pending approval.");
        }
        log.setStatus(WorkLog.WorkStatus.APPROVED);
        WorkLog savedLog = workLogRepository.save(log);
        return WorkLogResponse.fromEntity(savedLog);
    }

    private BigDecimal calculateWage(String wageType, BigDecimal wageRate, LocalDateTime checkIn, LocalDateTime checkOut) {
        // ... (This logic is unchanged)
        if ("DAILY".equals(wageType)) {
            return wageRate;
        } else if ("HOURLY".equals(wageType)) {
            long minutes = Duration.between(checkIn, checkOut).toMinutes();
            BigDecimal hours = new BigDecimal(minutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
            return wageRate.multiply(hours);
        }
        return BigDecimal.ZERO;
    }

    public List<WorkLogResponse> getMyWorkLogs() {
        // ... (This logic is unchanged from the last step)
        User user = getAuthenticatedUser();
        List<WorkLog> logs;
        if (user.getRole() == com.pehchaan.backend.entity.Role.ROLE_LABOR) {
            logs = workLogRepository.findByLaborerId(user.getId());
        } else if (user.getRole() == com.pehchaan.backend.entity.Role.ROLE_CONTRACTOR) {
            List<Long> projectIds = projectRepository.findByContractorId(user.getId())
                    .stream().map(Project::getId).collect(Collectors.toList());
            if (projectIds.isEmpty()) {
                logs = List.of();
            } else {
                logs = workLogRepository.findByProjectIdIn(projectIds);
            }
        } else {
            logs = List.of();
        }
        return logs.stream()
                   .map(WorkLogResponse::fromEntity)
                   .collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        // ... (This logic is unchanged)
        String userPhone = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userPhone));
    }
}