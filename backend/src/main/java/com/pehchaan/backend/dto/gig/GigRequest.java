package com.pehchaan.backend.dto.gig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GigRequest {
    private Long laborerId;
    private String skill;
    private double latitude;  // Consumer's latitude
    private double longitude; // Consumer's longitude
    private String address;   // Consumer's full address
}