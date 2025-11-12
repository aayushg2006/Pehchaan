package com.pehchaan.backend.dto.auth;

import com.pehchaan.backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String phone;
    private String password;
    private Role role; // This will be ROLE_LABOR, ROLE_CONTRACTOR, etc.
}