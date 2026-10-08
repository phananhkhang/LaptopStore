package org.akira.laptopstore.catalog.service;

import java.util.List;

import org.akira.laptopstore.catalog.dto.request.CreateVariantRequest;
import org.akira.laptopstore.catalog.dto.response.VariantSnapshot;
import org.akira.laptopstore.catalog.dto.request.UpdateVariantRequest;

public interface ProductVariantService {
    VariantSnapshot createVariant(Long productId, CreateVariantRequest request);

    VariantSnapshot updateVariant(Long productId, Long variantId, UpdateVariantRequest request);

// Hàm này không cần trả về cục dữ liệu to, chỉ cần chạy thành công là được nên dùng void
    void updateVariantActiveStatus(Long productId, Long variantId, boolean isActive);

    List<VariantSnapshot> getVariantsByProductId(Long productId);
    VariantSnapshot getVariantById(Long productId, Long variantId);
}