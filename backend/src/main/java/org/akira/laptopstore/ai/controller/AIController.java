package org.akira.laptopstore.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.ai.client.AIModelClient;
import org.akira.laptopstore.ai.dto.AIChatRequest;
import org.akira.laptopstore.ai.dto.AIChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIModelClient aiModelClient;

    @PostMapping("/chat")
    public ResponseEntity<AIChatResponse> chat(@Valid @RequestBody AIChatRequest request) {
        return ResponseEntity.ok(aiModelClient.chat(request));
    }
}
