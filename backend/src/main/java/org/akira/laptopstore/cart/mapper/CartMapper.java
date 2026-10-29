package org.akira.laptopstore.cart.mapper;

import org.akira.laptopstore.cart.dto.response.CartItemResponse;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.catalog.dto.response.CartVariantSnapshot;
import org.akira.laptopstore.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class CartMapper {
    private CartMapper() {
    }

    public static CartResponse toResponse(Cart cart, Map<Long, CartVariantSnapshot> variantsById) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    CartVariantSnapshot variant = variantsById.get(item.getVariantId());
                    if (variant == null) {
                        throw new ResourceNotFoundException("Product variant not found: " + item.getVariantId());
                    }
                    String productName = variant.getProductName();
                    String variantName = variant.getVariantName();
                    BigDecimal unitPrice = variant.getUnitPrice();
                    BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                    CartItemResponse response = new CartItemResponse(
                            item.getId(),
                            item.getVariantId(),
                            productName,
                            variant.getSku(),
                            variantName,
                            item.getQuantity(),
                            unitPrice,
                            lineTotal
                    );
                    response.setProductId(variant.getProductId());
                    response.setAvailableQuantity(variant.getQuantity());
                    response.setSellable(variant.isSellable());
                    return response;
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
