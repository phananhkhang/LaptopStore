package org.akira.laptopstore.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class UserCredentialDto {
    private Long id;
    private String email;
    private String passwordHash;
    private Integer tokenVersion;
    private String status;
    private Set<String> roles;
}
