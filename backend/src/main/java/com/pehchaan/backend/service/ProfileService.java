package com.pehchaan.backend.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.pehchaan.backend.dto.profile.ProfileResponse;
import com.pehchaan.backend.dto.profile.UpdateProfileRequest;
import com.pehchaan.backend.entity.User;
import com.pehchaan.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    /**
     * Gets the profile information for the *currently authenticated* user.
     */
    public ProfileResponse getMyProfile() {
        // 1. Get the currently logged-in user from the Security Context
        User user = getAuthenticatedUser();
        
        // 2. Map the User entity to a safe ProfileResponse DTO
        return mapUserToProfileResponse(user);
    }

    /**
     * Updates the profile for the *currently authenticated* user.
     */
    public ProfileResponse updateMyProfile(UpdateProfileRequest request) {
        // 1. Get the currently logged-in user
        User user = getAuthenticatedUser();

        // 2. Update the user's fields from the request
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setSkills(request.getSkills());
        
        // 3. Save the updated user back to the database
        User updatedUser = userRepository.save(user);

        // 4. Return the newly updated profile
        return mapUserToProfileResponse(updatedUser);
    }

    
    // --- Helper Methods ---

    /**
     * A private helper to get the User object for the currently logged-in user.
     * This is a very common and useful utility function.
     */
    private User getAuthenticatedUser() {
        String userPhone = SecurityContextHolder.getContext()
                                                .getAuthentication()
                                                .getName();
        
        return userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userPhone));
    }

    /**
     * A private helper to map a User entity (which has the password) to a 
     * safe-to-send ProfileResponse DTO.
     */
    private ProfileResponse mapUserToProfileResponse(User user) {
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
                // .currentLocation(user.getCurrentLocation()) // We can add this later
                .build();
    }
}