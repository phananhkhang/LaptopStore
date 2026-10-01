package org.akira.laptopstore.ai.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.akira.laptopstore.ai.client.AIModelClient;
import org.akira.laptopstore.ai.config.AIProperties;
import org.akira.laptopstore.ai.dto.AIChatRequest;
import org.akira.laptopstore.ai.dto.AIChatResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAIClient implements AIModelClient {

    private final AIProperties aiProperties;

    @Override
    public AIChatResponse chat(AIChatRequest request) {
        long startTime = System.currentTimeMillis();

        if (!aiProperties.isEnabled() || "mock-key".equalsIgnoreCase(aiProperties.getApiKey())) {
            log.info("AI provider is in mock/stub mode.");
            return AIChatResponse.builder()
                    .reply("LaptopStore AI Assistant: Cảm ơn bạn đã liên hệ. Hệ thống AI đang chạy với mô hình " + aiProperties.getModel() + " và sẵn sàng tiếp nhận câu hỏi của bạn.")
                    .model(aiProperties.getModel())
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }

        return AIChatResponse.builder()
                .reply("Phản hồi từ AI Model (" + aiProperties.getModel() + "): " + request.getPrompt())
                .model(aiProperties.getModel())
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }

    @Override
    public boolean isAvailable() {
        return aiProperties.isEnabled();
    }
}
