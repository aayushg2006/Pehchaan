package com.pehchaan.backend.config;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pehchaan.backend.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userPhone;

        // 1. Check if the request has a valid JWT header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // If not, pass to the next filter
            return;
        }

        // 2. Extract the token
        jwt = authHeader.substring(7); // "Bearer " is 7 chars

        // 3. Extract the phone number (username) from the token
        userPhone = jwtService.extractUsername(jwt);

        // 4. If we have a phone number AND the user is not already authenticated...
        if (userPhone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load the user from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userPhone);
            
            // 5. Check if the token is valid
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // If valid, create an authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // We don't have credentials
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // 6. Set this user as the currently authenticated user
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Pass to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}