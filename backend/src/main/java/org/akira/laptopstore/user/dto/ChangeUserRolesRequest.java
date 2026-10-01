package org.akira.laptopstore.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ChangeUserRolesRequest {
    @NotEmpty(message = "Roles must not be empty")
    private Set<String> roles;
}
