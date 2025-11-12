package com.pehchaan.backend.dto.assignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentRequest {
    private Long projectId;
    private Long laborerId;
    private BigDecimal wageRate;
    private String wageType; // "DAILY" or "HOURLY"
}