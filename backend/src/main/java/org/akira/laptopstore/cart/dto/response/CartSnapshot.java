package org.akira.laptopstore.cart.dto.response;

import java.util.List;

/** Internal checkout contract. The caller must hold the transaction until checkout completes. */
public record CartSnapshot(Long cartId, Long userId, List<Item> items) {
    public CartSnapshot {
        items = List.copyOf(items);
    }

    public record Item(Long cartItemId, Long variantId, Integer quantity) {
    }
}
