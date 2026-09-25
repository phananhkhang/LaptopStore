package org.akira.laptopstore.cart.mapper;

import org.akira.laptopstore.cart.dto.response.CartItemResponse;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.common.util.VariantUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class CartMapper {
    private CartMapper() {
    }

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
        return toResponse(cart, Map.of());
    }

    public static CartResponse toResponse(Cart cart, Map<Long, VariantSummary> variantsById) {
        if (cart == null) {
            return null;
        }
        Map<Long, VariantSummary> safeVariants = variantsById == null ? Map.of() : variantsById;
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    VariantSummary variant = safeVariants.get(item.getVariantId());
                    String productName = variant == null ? null : variant.productName();
                    String sku = variant == null ? null : variant.sku();
                    String ram = variant == null ? null : variant.ram();
                    String storage = variant == null ? null : variant.storage();
                    String color = variant == null ? null : variant.color();
                    String variantName = VariantUtils.formatFullProductName(productName, ram, storage, color);
                    BigDecimal unitPrice = variant == null || variant.price() == null
                            ? BigDecimal.ZERO
                            : variant.price();
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
