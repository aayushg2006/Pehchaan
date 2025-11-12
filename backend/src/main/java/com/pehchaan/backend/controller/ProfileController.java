package com.pehchaan.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // ✅ ADD
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pehchaan.backend.dto.profile.ProfileResponse;
import com.pehchaan.backend.dto.profile.UpdateLocationRequest; // ✅ ADD
import com.pehchaan.backend.dto.profile.UpdateProfileRequest;
import com.pehchaan.backend.dto.profile.UpdateStatusRequest; // ✅ ADD
import com.pehchaan.backend.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile") // Base URL for all profile endpoints
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        // ... (unchanged)
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @RequestBody UpdateProfileRequest request
    ) {
        // ... (unchanged)
        return ResponseEntity.ok(profileService.updateMyProfile(request));
    }

    /**
     * ✅ ADDED: Laborer updates their availability status.
     */
    @PutMapping("/me/status")
    @PreAuthorize("hasRole('LABOR')")
    public ResponseEntity<ProfileResponse> updateMyStatus(
            @RequestBody UpdateStatusRequest request
    ) {
        return ResponseEntity.ok(profileService.updateMyStatus(request));
    }

    /**
     * ✅ ADDED: Laborer updates their current location.
     */
    @PutMapping("/me/location")
    @PreAuthorize("hasRole('LABOR')")
    public ResponseEntity<ProfileResponse> updateMyLocation(
            @RequestBody UpdateLocationRequest request
    ) {
        return ResponseEntity.ok(profileService.updateMyLocation(request));
    }
}