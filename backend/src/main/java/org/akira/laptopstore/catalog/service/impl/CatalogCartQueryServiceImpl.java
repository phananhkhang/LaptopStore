package org.akira.laptopstore.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.catalog.dto.response.CartVariantSnapshot;
import org.akira.laptopstore.catalog.entity.ProductVariant;
import org.akira.laptopstore.catalog.repository.ProductVariantRepository;
import org.akira.laptopstore.catalog.service.CatalogCartQueryService;
import org.akira.laptopstore.common.util.VariantUtils;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogCartQueryServiceImpl implements CatalogCartQueryService {
    private final ProductVariantRepository variants;

    @Override
    public CartVariantSnapshot getVariant(Long variantId) {
        return toSnapshot(variants.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found: " + variantId)));
    }

    @Override
    public List<CartVariantSnapshot> getVariants(Collection<Long> variantIds) {
        return variants.findAllById(variantIds).stream().map(this::toSnapshot).toList();
    }

    private CartVariantSnapshot toSnapshot(ProductVariant variant) {
        if (variant.getProduct() == null) {
            throw new ResourceNotFoundException("Product variant not found: " + variant.getId());
        }
        String productName = variant.getProduct().getName();
        boolean sellable = "ACTIVE".equalsIgnoreCase(variant.getStatus())
                && "ACTIVE".equalsIgnoreCase(variant.getProduct().getStatus());
        return new CartVariantSnapshot(variant.getId(), productName,
                VariantUtils.formatFullProductName(productName, variant.getRam(), variant.getStorage(), variant.getColor()),
                variant.getSku(), variant.getPrice(), variant.getQuantity(), sellable);
    }
}
