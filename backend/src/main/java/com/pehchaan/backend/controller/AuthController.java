package com.pehchaan.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pehchaan.backend.dto.auth.AuthenticationResponse;
import com.pehchaan.backend.dto.auth.LoginRequest;
import com.pehchaan.backend.dto.auth.RegisterRequest;
import com.pehchaan.backend.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth") // Base URL for all auth endpoints
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        // Call the service to register the user and return the token
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody LoginRequest request
    ) {
        // Call the service to log in the user and return the token
        return ResponseEntity.ok(authService.login(request));
    }
}