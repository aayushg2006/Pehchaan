package com.pehchaan.backend.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory; // ✅ IMPORT
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.pehchaan.backend.dto.profile.ProfileResponse;
import com.pehchaan.backend.dto.profile.UpdateLocationRequest;
import com.pehchaan.backend.dto.profile.UpdateProfileRequest;
import com.pehchaan.backend.dto.profile.UpdateStatusRequest;
import com.pehchaan.backend.entity.User;
import com.pehchaan.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory; // ✅ ADD THIS LINE

    public ProfileResponse getMyProfile() {
        User user = getAuthenticatedUser();
        return mapUserToProfileResponse(user);
    }

    public ProfileResponse updateMyProfile(UpdateProfileRequest request) {
        User user = getAuthenticatedUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setSkills(request.getSkills());
        
        User updatedUser = userRepository.save(user);
        return mapUserToProfileResponse(updatedUser);
    }

    public ProfileResponse updateMyStatus(UpdateStatusRequest request) {
        User user = getAuthenticatedUser();
        user.setStatus(request.getStatus());
        User updatedUser = userRepository.save(user);
        return mapUserToProfileResponse(updatedUser); 
    }

    public ProfileResponse updateMyLocation(UpdateLocationRequest request) {
        User user = getAuthenticatedUser();
        
        // This was failing with NullPointerException before
        var locationPoint = geometryFactory.createPoint(
            new Coordinate(request.getLongitude(), request.getLatitude())
        );
        user.setCurrentLocation(locationPoint);
        
        User updatedUser = userRepository.save(user);
        return mapUserToProfileResponse(updatedUser);
    }

    
    // --- Helper Methods ---
    private User getAuthenticatedUser() {
        String userPhone = SecurityContextHolder.getContext()
                                                .getAuthentication()
                                                .getName();
        
        return userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userPhone));
    }

    private ProfileResponse mapUserToProfileResponse(User user) {
        // This relies on the ProfileResponse.fromEntity() method
        return ProfileResponse.fromEntity(user);
    }
}