package org.akira.laptopstore.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private Long userId;
    private String email;
    private String fullName;
    private Set<String> roles;
}
