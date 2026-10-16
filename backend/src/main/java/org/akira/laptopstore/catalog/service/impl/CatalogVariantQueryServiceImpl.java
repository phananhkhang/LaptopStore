package org.akira.laptopstore.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.catalog.dto.response.VariantStockSnapshot;
import org.akira.laptopstore.catalog.entity.ProductVariant;
import org.akira.laptopstore.catalog.repository.ProductVariantRepository;
import org.akira.laptopstore.catalog.service.CatalogVariantQueryService;
import org.akira.laptopstore.common.util.VariantUtils;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogVariantQueryServiceImpl implements CatalogVariantQueryService {

    private final ProductVariantRepository productVariantRepository;

    @Override
    public VariantStockSnapshot getVariantStock(Long variantId) {
        if (variantId == null || variantId <= 0) {
            throw new ValidationException("variantId phải lớn hơn 0");
        }
        ProductVariant variant = productVariantRepository.findByIdWithProduct(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với ID: " + variantId));
        return toSnapshot(variant);
    }

    @Override
    public List<VariantStockSnapshot> getVariantStocks(Collection<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return List.of();
        }
        return productVariantRepository.findAllByIdInWithProduct(variantIds).stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    public List<VariantStockSnapshot> getAllVariantStocks() {
        return productVariantRepository.findAllWithProductOrderByQuantityAsc().stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    public List<VariantStockSnapshot> getLowStockVariants(int threshold) {
        if (threshold < 0) {
            throw new ValidationException("Ngưỡng tồn kho thấp (threshold) không được âm");
        }
        return productVariantRepository.findLowStockVariants(threshold).stream()
                .map(this::toSnapshot)
                .toList();
    }

    private VariantStockSnapshot toSnapshot(ProductVariant variant) {
        Long productId = variant.getProduct() != null ? variant.getProduct().getId() : null;
        String productName = variant.getProduct() != null ? variant.getProduct().getName() : null;

        return VariantStockSnapshot.builder()
                .variantId(variant.getId())
                .productId(productId)
                .productName(productName)
                .variantName(VariantUtils.formatVariantName(variant.getRam(), variant.getStorage(), variant.getColor()))
                .sku(variant.getSku())
                .ram(variant.getRam())
                .storage(variant.getStorage())
                .color(variant.getColor())
                .price(variant.getPrice())
                .quantity(variant.getQuantity() == null ? 0 : variant.getQuantity())
                .status(variant.getStatus())
                .active(variant.isActive())
                .build();
    }
}