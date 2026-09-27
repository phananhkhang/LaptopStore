package org.akira.laptopstore.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIChatRequest {
    @NotBlank(message = "Prompt cannot be empty")
    private String prompt;
    private String systemInstruction;
}
