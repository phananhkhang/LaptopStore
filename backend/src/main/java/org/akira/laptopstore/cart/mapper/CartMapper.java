package org.akira.laptopstore.cart.mapper;

import org.akira.laptopstore.cart.dto.response.CartItemResponse;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.common.util.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CartMapper {
    public record VariantSummary(
            Long id,
            String productName,
            String sku,
            String ram,          // VD: "16GB"
            String storage,      // VD: "512GB"
            String color,        // VD: "Xám không gian"
            BigDecimal price
    ) {}
    public static CartResponse toResponse(Cart cart) {
        if (cart == null) {
            return null;
        }
        Map<Long, VariantSummary> variantMap = Map.of();
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    VariantSummary variant = variantMap.get(item.getVariantId());
                    String productName = (variant != null) ? variant.productName() : null;
                    String sku = (variant != null) ? variant.sku() : null;
                    String ram = (variant != null) ? variant.ram() : null;
                    String storage = (variant != null) ? variant.storage() : null;
                    String color = (variant != null) ? variant.color() : null;
                    String variantName = VariantUtils.formatFullProductName(productName, ram, storage, color);
                    BigDecimal unitPrice = (variant != null) ? variant.price() : BigDecimal.ZERO;
                    BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new CartItemResponse(
                            item.getVariantId(),
                            productName,
                            sku,
                            variantName,
                            item.getQuantity(),
                            unitPrice,
                            lineTotal
                    );
                }).toList();
        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();
        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(
                cart.getId(),
                itemResponses,
                totalItems,
                totalAmount
        );
    }
}
