package org.akira.laptopstore.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.catalog.dto.response.CheckoutVariantSnapshot;
import org.akira.laptopstore.catalog.dto.response.StockChangeResult;
import org.akira.laptopstore.catalog.entity.ProductVariant;
import org.akira.laptopstore.catalog.repository.ProductVariantRepository;
import org.akira.laptopstore.catalog.service.CatalogCheckoutService;
import org.akira.laptopstore.common.util.VariantUtils;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogCheckoutServiceImpl implements CatalogCheckoutService {
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<CheckoutVariantSnapshot> lockAndGetSellableVariants(Collection<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty() || variantIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new ValidationException("variantIds must contain positive IDs");
        }
        List<Long> orderedIds = variantIds.stream().distinct().sorted().toList();
        List<ProductVariant> variants = productVariantRepository.findAllByIdInForUpdate(orderedIds);
        if (variants.size() != orderedIds.size()) {
            throw new ResourceNotFoundException("One or more product variants were not found");
        }

        List<CheckoutVariantSnapshot> snapshots = new ArrayList<>(variants.size());
        for (ProductVariant variant : variants) {
            if (!"ACTIVE".equalsIgnoreCase(variant.getStatus())
                    || variant.getProduct() == null
                    || !"ACTIVE".equalsIgnoreCase(variant.getProduct().getStatus())) {
                throw new ValidationException("Product variant is not available: " + variant.getId());
            }
            snapshots.add(new CheckoutVariantSnapshot(
                    variant.getId(), variant.getProduct().getId(), variant.getProduct().getName(),
                    VariantUtils.formatVariantName(variant.getRam(), variant.getStorage(), variant.getColor()),
                    variant.getSku(), variant.getPrice()));
        }
        return List.copyOf(snapshots);
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
}
