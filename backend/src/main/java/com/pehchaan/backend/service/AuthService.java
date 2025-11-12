package com.pehchaan.backend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pehchaan.backend.dto.auth.AuthenticationResponse;
import com.pehchaan.backend.dto.auth.LoginRequest;
import com.pehchaan.backend.dto.auth.RegisterRequest;
import com.pehchaan.backend.entity.User;
import com.pehchaan.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        // 1. Create a new User object from the request
        var user = User.builder()
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword())) // Hash the password
                .role(request.getRole())
                .isVerified(false) // ✅ FIX: Set default value
                .build();

        // 2. Save the new user to the database
        User savedUser = userRepository.save(user);

        // 3. Generate a JWT for the new user
        var jwtToken = jwtService.generateToken(savedUser);

        // 4. Return the token and role in the response
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(savedUser.getRole().name())
                .build();
    }

    public AuthenticationResponse login(LoginRequest request) {
        // 1. Authenticate the user (check if phone and password are correct)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getPhone(),
                        request.getPassword()
                )
        );

        // 2. If authentication is successful, find the user
        var user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(); // We know the user exists at this point

        // 3. Generate a JWT for them
        var jwtToken = jwtService.generateToken(user);

        // 4. Return the token and role
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(user.getRole().name())
                .build();
    }
}