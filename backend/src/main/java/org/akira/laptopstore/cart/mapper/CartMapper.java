package org.akira.laptopstore.cart.mapper;

import org.akira.laptopstore.cart.dto.response.CartItemResponse;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.entity.Cart;

import java.util.List;

public final class CartMapper {
    private CartMapper() {
    }

    /** Temporary response until catalog provides its variant DTO contract. */
    public static CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    return new CartItemResponse(
                            item.getId(),
                            item.getVariantId(),
                            null,
                            null,
                            null,
                            item.getQuantity(),
                            null,
                            null
                    );
                }).toList();
        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();
        return new CartResponse(
                cart.getId(),
                itemResponses,
                totalItems,
                null
        );
    }
}
