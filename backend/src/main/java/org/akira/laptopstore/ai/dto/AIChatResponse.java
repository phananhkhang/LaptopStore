package org.akira.laptopstore.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatResponse {
    private String reply;
    private String model;
    private long executionTimeMs;
}
