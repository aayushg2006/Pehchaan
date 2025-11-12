package com.pehchaan.backend.dto.profile;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    // These are the fields we'll ask for on the "Complete Profile" page
    private String firstName;
    private String lastName;
    private Set<String> skills;
    // We'll add location, etc. later
}