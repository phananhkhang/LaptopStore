package org.akira.laptopstore.catalog.service.impl;

import lombok.RequiredArgsConstructor;

import org.akira.laptopstore.catalog.dto.response.CheckoutVariantSnapshot;
import org.akira.laptopstore.catalog.dto.response.StockChangeResult;

import org.akira.laptopstore.catalog.entity.ProductVariant;
import org.akira.laptopstore.catalog.repository.ProductVariantRepository;
import org.akira.laptopstore.catalog.service.CatalogCheckoutService;

import org.akira.laptopstore.common.util.VariantUtils;
import org.akira.laptopstore.exception.BusinessException;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogCheckoutServiceImpl implements CatalogCheckoutService {
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<CheckoutVariantSnapshot> lockAndGetSellableVariants(Collection<Long> variantIds) {
        List<Long> orderedIds = validateAndOrderIds(variantIds);
        List<ProductVariant> variants = productVariantRepository.findAllByIdInForUpdate(orderedIds);
        return toSellableSnapshots(orderedIds, variants);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckoutVariantSnapshot> getSellableVariants(Collection<Long> variantIds) {
        List<Long> orderedIds = validateAndOrderIds(variantIds);
        List<ProductVariant> variants = productVariantRepository.findAllById(orderedIds).stream()
                .sorted(Comparator.comparing(ProductVariant::getId))
                .toList();
        return toSellableSnapshots(orderedIds, variants);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public StockChangeResult deductStock(Long variantId, int quantity) {
        if (variantId == null || variantId <= 0 || quantity <= 0) {
            throw new ValidationException("variantId and quantity must be greater than 0");
        }
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found: " + variantId));
        int before = variant.getQuantity() == null ? 0 : variant.getQuantity();
        if (before < quantity) {
            throw new ValidationException("Not enough stock for product variant: " + variantId);
        }
        variant.setQuantity(before - quantity);
        return new StockChangeResult(variantId, before, before - quantity);
    }

    @Override
    @Transactional
    public StockChangeResult adjustStock(Long variantId, int delta) {
        // SỬ DỤNG FINDBYIDFORUPDATE ĐỂ KHÓA DÒNG DỮ LIỆU
        ProductVariant variant = productVariantRepository.findByIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Variant ID: " + variantId));

        int quantityBefore = variant.getQuantity();
        int quantityAfter = quantityBefore + delta;

        // Chặn luồng bán hàng nếu trừ âm kho (tùy nghiệp vụ của bạn)
        if (quantityAfter < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "OUT_OF_STOCK", 
                    "Không đủ số lượng tồn kho để thực hiện giao dịch.");
        }

        variant.setQuantity(quantityAfter);
        productVariantRepository.save(variant);

        return new StockChangeResult(variantId, quantityBefore, quantityAfter);
    }

    private List<Long> validateAndOrderIds(Collection<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty() || variantIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new ValidationException("variantIds must contain positive IDs");
        }
        return variantIds.stream().distinct().sorted().toList();
    }

    private List<CheckoutVariantSnapshot> toSellableSnapshots(List<Long> orderedIds, List<ProductVariant> variants) {
        if (variants.size() != orderedIds.size()) {
            throw new ResourceNotFoundException("One or more product variants were not found");
        }

        List<CheckoutVariantSnapshot> snapshots = new ArrayList<>(variants.size());
        for (ProductVariant variant : variants) {
            // Kiểm tra xem biến thể có sẵn sàng để bán không
            if (!"ACTIVE".equalsIgnoreCase(variant.getStatus())
                    || variant.getProduct() == null
                    || !"ACTIVE".equalsIgnoreCase(variant.getProduct().getStatus())) {
                throw new ValidationException("Product variant is not available: " + variant.getId());
            }
            int availableQuantity = variant.getQuantity() == null ? 0 : variant.getQuantity();
            snapshots.add(new CheckoutVariantSnapshot(
                    variant.getId(),
                    variant.getProduct().getId(),
                    variant.getProduct().getName(),
                    VariantUtils.formatVariantName(variant.getRam(), variant.getStorage(), variant.getColor()),
                    variant.getSku(),
                    variant.getPrice(),
                    availableQuantity
            ));
        }
        return List.copyOf(snapshots);
    }
}
