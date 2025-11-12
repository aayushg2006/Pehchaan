package com.pehchaan.backend.dto.project;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProjectRequest {
    private String name;
    private String address;
    private double latitude; // We'll get this from the Google Maps pin
    private double longitude; // We'll get this from the Google Maps pin
    private BigDecimal wageRate;
    private String wageType; // "DAILY" or "HOURLY"
}