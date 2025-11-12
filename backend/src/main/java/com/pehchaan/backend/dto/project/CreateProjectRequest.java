package com.pehchaan.backend.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// import java.math.BigDecimal; // No longer needed

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProjectRequest {
    private String name;
    private String address;
    private double latitude; // We'll get this from the map pin
    private double longitude; // We'll get this from the map pin
    // ❌ Wage fields removed
    // private BigDecimal wageRate;
    // private String wageType; 
}