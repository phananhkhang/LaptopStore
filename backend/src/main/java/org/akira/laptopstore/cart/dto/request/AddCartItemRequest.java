package org.akira.laptopstore.cart.dto.request;

public record AddCartItemRequest(
        Long productVariantId,
        Integer quantity
) {
}
