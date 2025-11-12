package com.pehchaan.backend.dto.assignment;

import com.pehchaan.backend.entity.Assignment;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AssignmentResponse {
    private Long id;
    private Long projectId;
    private Long laborerId;
    private BigDecimal wageRate;
    private String wageType;

    // Denormalized project data for easy frontend display
    private String projectName;
    private String projectAddress;
    private double projectLatitude;
    private double projectLongitude;

    public static AssignmentResponse fromEntity(Assignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .projectId(assignment.getProject().getId())
                .laborerId(assignment.getLaborer().getId())
                .wageRate(assignment.getWageRate())
                .wageType(assignment.getWageType())
                .projectName(assignment.getProject().getName())
                .projectAddress(assignment.getProject().getAddress())
                .projectLatitude(assignment.getProject().getLocation().getY()) // Lat is Y
                .projectLongitude(assignment.getProject().getLocation().getX()) // Lon is X
                .build();
    }
}