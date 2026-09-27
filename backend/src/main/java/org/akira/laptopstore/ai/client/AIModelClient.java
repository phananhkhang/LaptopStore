package org.akira.laptopstore.ai.client;

import org.akira.laptopstore.ai.dto.AIChatRequest;
import org.akira.laptopstore.ai.dto.AIChatResponse;

public interface AIModelClient {
    AIChatResponse chat(AIChatRequest request);
    boolean isAvailable();
}
