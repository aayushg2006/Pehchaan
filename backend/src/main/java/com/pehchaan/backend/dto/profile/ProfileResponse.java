package com.pehchaan.backend.dto.profile;

import java.util.Set;
import com.pehchaan.backend.entity.Role;
import com.pehchaan.backend.entity.User; // ✅ IMPORT User
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
    private Boolean isVerified; 
    private String status;
    private Set<String> skills;
    // ✅ ADD: We need to send the location for the consumer map
    private Double latitude;
    private Double longitude;

    /**
     * ✅ ADDED: A static helper to map from the entity.
     * This is used by ProfileService and AssignmentService.
     */
    public static ProfileResponse fromEntity(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .rating(user.getRating())
                .isVerified(user.getIsVerified())
                .status(user.getStatus())
                .skills(user.getSkills())
                // ✅ ADD: Handle null location
                .latitude(user.getCurrentLocation() != null ? user.getCurrentLocation().getY() : null)
                .longitude(user.getCurrentLocation() != null ? user.getCurrentLocation().getX() : null)
                .build();
    }
}