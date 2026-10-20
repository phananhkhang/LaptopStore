package org.akira.laptopstore.ai.service;

import org.akira.laptopstore.ai.client.AIModelClient;
import org.akira.laptopstore.ai.config.AIProperties;
import org.akira.laptopstore.ai.dto.AIChatRequest;
import org.akira.laptopstore.ai.dto.AIChatResponse;
import org.akira.laptopstore.ai.service.impl.AIServiceImpl;
import org.akira.laptopstore.catalog.dto.response.VariantStockSnapshot;
import org.akira.laptopstore.catalog.service.CatalogVariantQueryService;
import org.akira.laptopstore.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceImplTest {

    @Mock
    private AIModelClient aiModelClient;

    @Mock
    private CatalogVariantQueryService catalogVariantQueryService;

    @Mock
    private AIProperties aiProperties;

    @InjectMocks
    private AIServiceImpl aiService;

    private VariantStockSnapshot sampleVariant;

    @BeforeEach
    void setUp() {
        sampleVariant = VariantStockSnapshot.builder()
                .variantId(101L)
                .productId(10L)
                .productName("ThinkPad T14 Gen 4")
                .variantName("Core i7 / 16GB / 512GB SSD")
                .sku("TP-T14-01")
                .ram("16GB")
                .storage("512GB SSD")
                .color("Đen")
                .price(new BigDecimal("22500000"))
                .quantity(15)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should inject catalog context and call AI client when prompt is valid")
    void chat_withValidPrompt_shouldInjectCatalogContext() {
        when(catalogVariantQueryService.getAllVariantStocks()).thenReturn(List.of(sampleVariant));
        when(aiModelClient.isAvailable()).thenReturn(true);
        when(aiModelClient.chat(any(AIChatRequest.class))).thenReturn(
                AIChatResponse.builder()
                        .reply("Chúng tôi có dòng máy ThinkPad T14 Gen 4 phù hợp với nhu cầu của bạn.")
                        .model("gemini-2.0-flash")
                        .executionTimeMs(120L)
                        .build()
        );

        AIChatRequest request = AIChatRequest.builder()
                .prompt("Tư vấn laptop văn phòng khoảng 20-25 triệu bền bỉ")
                .build();

        AIChatResponse response = aiService.chat(request);

        assertThat(response).isNotNull();
        assertThat(response.getReply()).contains("ThinkPad T14");

        ArgumentCaptor<AIChatRequest> captor = ArgumentCaptor.forClass(AIChatRequest.class);
        verify(aiModelClient).chat(captor.capture());
        AIChatRequest sentRequest = captor.getValue();
        assertThat(sentRequest.getPrompt()).isEqualTo(request.getPrompt());
        assertThat(sentRequest.getSystemInstruction()).contains("LaptopStore");
        assertThat(sentRequest.getSystemInstruction()).contains("ThinkPad T14 Gen 4");
        assertThat(sentRequest.getSystemInstruction()).contains("22500000");
    }

    @Test
    @DisplayName("Should throw ValidationException when prompt is null or blank")
    void chat_withBlankPrompt_shouldThrowValidationException() {
        AIChatRequest emptyRequest = AIChatRequest.builder().prompt("   ").build();

        assertThatThrownBy(() -> aiService.chat(emptyRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Prompt cannot be empty");

        assertThatThrownBy(() -> aiService.chat(AIChatRequest.builder().prompt(null).build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Should still proceed with base system instruction when catalog has no active variants")
    void chat_whenCatalogEmpty_shouldProceedWithBasePrompt() {
        when(catalogVariantQueryService.getAllVariantStocks()).thenReturn(Collections.emptyList());
        when(aiModelClient.isAvailable()).thenReturn(true);
        when(aiModelClient.chat(any(AIChatRequest.class))).thenReturn(
                AIChatResponse.builder()
                        .reply("Hiện tại danh mục sản phẩm đang được cập nhật, tôi có thể tư vấn tiêu chí chọn máy cho bạn.")
                        .model("gemini-2.0-flash")
                        .executionTimeMs(90L)
                        .build()
        );

        AIChatRequest request = AIChatRequest.builder()
                .prompt("Có máy nào nhẹ dưới 1.3kg không?")
                .build();

        AIChatResponse response = aiService.chat(request);

        assertThat(response).isNotNull();
        ArgumentCaptor<AIChatRequest> captor = ArgumentCaptor.forClass(AIChatRequest.class);
        verify(aiModelClient).chat(captor.capture());
        assertThat(captor.getValue().getSystemInstruction()).contains("LaptopStore");
    }

    @Test
    @DisplayName("Should return friendly fallback response when AI model client is unavailable")
    void chat_whenModelClientUnavailable_shouldReturnFallback() {
        when(aiModelClient.isAvailable()).thenReturn(false);

        AIChatRequest request = AIChatRequest.builder()
                .prompt("Tư vấn máy gaming")
                .build();

        AIChatResponse response = aiService.chat(request);

        assertThat(response).isNotNull();
        assertThat(response.getReply()).contains("bảo trì");
        verify(aiModelClient, never()).chat(any());
    }
}
