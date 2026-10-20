package org.akira.laptopstore.ai.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.akira.laptopstore.ai.client.AIModelClient;
import org.akira.laptopstore.ai.config.AIProperties;
import org.akira.laptopstore.ai.dto.AIChatRequest;
import org.akira.laptopstore.ai.dto.AIChatResponse;
import org.akira.laptopstore.ai.service.AIService;
import org.akira.laptopstore.catalog.dto.response.VariantStockSnapshot;
import org.akira.laptopstore.catalog.service.CatalogVariantQueryService;
import org.akira.laptopstore.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final AIModelClient aiModelClient;
    private final CatalogVariantQueryService catalogVariantQueryService;
    private final AIProperties aiProperties;

    @Override
    public AIChatResponse chat(AIChatRequest request) {
        if (request == null || request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            throw new ValidationException("Prompt cannot be empty");
        }

        if (!aiModelClient.isAvailable()) {
            log.warn("AI model client is not available.");
            return AIChatResponse.builder()
                    .reply("Hệ thống trợ lý AI hiện đang bảo trì hoặc chưa sẵn sàng. Vui lòng quay lại sau.")
                    .model(aiProperties != null ? aiProperties.getModel() : "unknown")
                    .executionTimeMs(0)
                    .build();
        }

        String systemInstruction = buildSystemInstruction(request.getSystemInstruction());

        AIChatRequest enrichedRequest = AIChatRequest.builder()
                .prompt(request.getPrompt().trim())
                .systemInstruction(systemInstruction)
                .build();

        return aiModelClient.chat(enrichedRequest);
    }

    private String buildSystemInstruction(String customInstruction) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý AI chuyên nghiệp và thân thiện của LaptopStore. ")
          .append("Nhiệm vụ của bạn là tư vấn các mẫu máy tính xách tay (laptop) phù hợp với nhu cầu, ngân sách và sở thích của khách hàng.\n\n");

        try {
            List<VariantStockSnapshot> variants = catalogVariantQueryService.getAllVariantStocks();
            if (variants != null && !variants.isEmpty()) {
                sb.append("Dưới đây là danh mục các sản phẩm laptop thực tế hiện có trong kho của LaptopStore:\n");
                for (VariantStockSnapshot v : variants) {
                    if (v.isActive()) {
                        sb.append(String.format("- %s (%s) | SKU: %s | RAM: %s | Ổ cứng: %s | Màu: %s | Giá: %s VND | Tồn kho: %d\n",
                                v.getProductName(),
                                v.getVariantName() != null ? v.getVariantName() : "",
                                v.getSku(),
                                v.getRam() != null ? v.getRam() : "N/A",
                                v.getStorage() != null ? v.getStorage() : "N/A",
                                v.getColor() != null ? v.getColor() : "N/A",
                                v.getPrice() != null ? v.getPrice().toPlainString() : "Liên hệ",
                                v.getQuantity() != null ? v.getQuantity() : 0));
                    }
                }
                sb.append("\nHãy ưu tiên tư vấn các sản phẩm có trong danh sách trên. Nêu rõ ưu điểm, cấu hình, giá thành và đối tượng phù hợp.\n");
            }
        } catch (Exception e) {
            log.warn("Không thể tải danh mục sản phẩm cho AI context: {}", e.getMessage());
        }

        if (customInstruction != null && !customInstruction.trim().isEmpty()) {
            sb.append("\nChỉ dẫn bổ sung: ").append(customInstruction.trim()).append("\n");
        }

        return sb.toString();
    }
}
