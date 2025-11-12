package com.pehchaan.backend.dto.work;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull; // ✅ IMPORT THIS

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckInRequest {
    
    @NonNull // ✅ ADD THIS ANNOTATION
    private Long projectId;
    
    private double latitude; // Primitives can't be null
    private double longitude;
}