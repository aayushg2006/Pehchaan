package com.pehchaan.backend.entity;

import java.util.Collection;
import java.util.List;
import java.util.Set; 

import org.locationtech.jts.geom.Point; 
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
import jakarta.persistence.OneToMany; // ✅ ADD
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
    private String firstName;
    private String lastName;
    
    private Double rating;
    
    // ✅ FIX: Add @Builder.Default to respect the default value
    @Builder.Default
    @Column(columnDefinition = "boolean default false")
    private Boolean isVerified = false;
    
    // For Laborers: "AVAILABLE", "OFFLINE", "ON_CONTRACT"
    private String status; 
    
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point currentLocation;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private Set<String> skills; // e.g., ["PLUMBER", "ELECTRICIAN"]

    // ✅ ADD: Link to the assignments this user has
    @OneToMany(mappedBy = "laborer")
    private Set<Assignment> assignments;
    
    // --- UserDetails Methods (Required by Spring Security) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
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