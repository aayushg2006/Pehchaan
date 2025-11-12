package com.pehchaan.backend.dto.project;

import com.pehchaan.backend.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private Long contractorId;
    private BigDecimal wageRate;
    private String wageType;

    /**
     * A helper method to convert a Project Entity into this DTO.
     */
    public static ProjectResponse fromEntity(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .address(project.getAddress())
                .latitude(project.getLocation().getY()) // In PostGIS, Y is Latitude
                .longitude(project.getLocation().getX()) // In PostGIS, X is Longitude
                .contractorId(project.getContractor().getId())
                .wageRate(project.getWageRate())
                .wageType(project.getWageType())
                .build();
    }
}