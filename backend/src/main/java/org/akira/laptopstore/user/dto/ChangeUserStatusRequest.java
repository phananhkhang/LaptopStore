package org.akira.laptopstore.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeUserStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
}
