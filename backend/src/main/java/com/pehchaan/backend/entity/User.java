package com.pehchaan.backend.entity;

import java.util.Collection;
import java.util.List;
import java.util.Set; // Import Set for skills

import org.locationtech.jts.geom.Point; // Import the PostGIS Point type
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- AUTH FIELDS (from Phase 1) ---
    @Column(unique = true, nullable = false)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // --- NEW PROFILE FIELDS (for Phase 2) ---
    // These will be null after registration
    
    private String firstName;
    private String lastName;
    
    // For the "Pehchaan Verified" Score
    private Double rating;
    @Column(columnDefinition = "boolean default false")
    private Boolean isVerified = false;
    
    // For Laborers: "Available", "Offline", "On_Contract"
    private String status; 
    
    // For the "Nearby Worker" feature
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point currentLocation;

    // For Laborers: Stores a list of their skills
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private Set<String> skills; // e.g., ["PLUMBER", "ELECTRICIAN"]

    
    // --- UserDetails Methods (Required by Spring Security) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        // For our app, the "username" is the phone number.
        return phone;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}