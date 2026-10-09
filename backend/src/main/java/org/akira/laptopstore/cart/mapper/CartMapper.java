package org.akira.laptopstore.cart.mapper;

import org.akira.laptopstore.cart.dto.response.CartItemResponse;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.catalog.entity.ProductVariant;
import org.akira.laptopstore.common.util.VariantUtils;
import org.akira.laptopstore.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class CartMapper {
    private CartMapper() {
    }

    public static CartResponse toResponse(Cart cart, Map<Long, ProductVariant> variantsById) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    ProductVariant variant = variantsById.get(item.getVariantId());
                    if (variant == null || variant.getProduct() == null) {
                        throw new ResourceNotFoundException("Product variant not found: " + item.getVariantId());
                    }
                    String productName = variant.getProduct().getName();
                    String variantName = VariantUtils.formatFullProductName(productName,
                            variant.getRam(), variant.getStorage(), variant.getColor());
                    BigDecimal unitPrice = variant.getPrice();
                    BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new CartItemResponse(
                            item.getId(),
                            item.getVariantId(),
                            productName,
                            variant.getSku(),
                            variantName,
                            item.getQuantity(),
                            unitPrice,
                            lineTotal
                    );
                }).toList();
        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();
        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(
                cart.getId(),
                itemResponses,
                totalItems,
                totalAmount
        );
    }
}
