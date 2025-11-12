package com.pehchaan.backend.dto.work;

import com.pehchaan.backend.entity.WorkLog;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WorkLogResponse {
    private Long id;
    private Long projectId;
    private Long laborerId;
    private String laborerName; // Nice to have
    private String projectName; // Nice to have
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal wageEarned;
    private WorkLog.WorkStatus status;

    public static WorkLogResponse fromEntity(WorkLog log) {
        return WorkLogResponse.builder()
                .id(log.getId())
                .projectId(log.getProject().getId())
                .projectName(log.getProject().getName())
                .laborerId(log.getLaborer().getId())
                .laborerName(log.getLaborer().getFirstName() + " " + log.getLaborer().getLastName())
                .checkInTime(log.getCheckInTime())
                .checkOutTime(log.getCheckOutTime())
                .wageEarned(log.getWageEarned())
                .status(log.getStatus())
                .build();
    }
}