package org.akira.laptopstore.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.akira.laptopstore.ai.dto.AIChatRequest;
import org.akira.laptopstore.ai.dto.AIChatResponse;
import org.akira.laptopstore.ai.service.AIService;
import org.akira.laptopstore.security.CustomAccessDeniedHandler;
import org.akira.laptopstore.security.JwtAuthenticationEntryPoint;
import org.akira.laptopstore.security.SecurityConfig;
import org.akira.laptopstore.security.filter.JwtAuthenticationFilter;
import org.akira.laptopstore.security.jwt.JwtTokenProvider;
import org.akira.laptopstore.security.principal.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AIController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class AIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AIService aiService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /api/v1/ai/chat should be publicly accessible and return 200 OK")
    void chat_publicEndpoint_shouldReturnOk() throws Exception {
        AIChatRequest request = AIChatRequest.builder()
                .prompt("Tư vấn laptop văn phòng mỏng nhẹ")
                .build();

        AIChatResponse response = AIChatResponse.builder()
                .reply("Bạn có thể tham khảo ThinkPad T14 Gen 4.")
                .model("gemini-2.0-flash")
                .executionTimeMs(150L)
                .build();

        when(aiService.chat(any(AIChatRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Bạn có thể tham khảo ThinkPad T14 Gen 4."))
                .andExpect(jsonPath("$.model").value("gemini-2.0-flash"));
    }

    @Test
    @DisplayName("POST /api/v1/ai/chat with empty prompt should return 400 Bad Request")
    void chat_withEmptyPrompt_shouldReturnBadRequest() throws Exception {
        AIChatRequest request = AIChatRequest.builder()
                .prompt("")
                .build();

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
