package com.pehchaan.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pehchaan.backend.dto.profile.ProfileResponse;
import com.pehchaan.backend.dto.profile.UpdateProfileRequest;
import com.pehchaan.backend.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile") // Base URL for all profile endpoints
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * GET /api/profile/me
     * Fetches the profile of the currently logged-in user.
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        // Our JwtAuthFilter secures this endpoint.
        // The service will get the user from the security context.
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    /**
     * PUT /api/profile/me
     * Updates the profile of the currently logged-in user.
     */
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @RequestBody UpdateProfileRequest request
    ) {
        // The service handles finding the right user and updating them
        return ResponseEntity.ok(profileService.updateMyProfile(request));
    }
}