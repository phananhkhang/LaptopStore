package org.akira.laptopstore.ai.service;

import org.akira.laptopstore.ai.dto.AIChatRequest;
import org.akira.laptopstore.ai.dto.AIChatResponse;

public interface AIService {
    AIChatResponse chat(AIChatRequest request);
}
