package com.pehchaan.backend.dto.profile;

import java.util.Set;
import com.pehchaan.backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    
    private Long id;
    private String phone;
    private Role role;
    private String firstName;
    private String lastName;
    private Double rating;
    private Boolean isVerified; // ✅ FIX: Changed from boolean to Boolean
    private String status;
    private Set<String> skills;
}