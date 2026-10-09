package org.akira.laptopstore.cart.dto.response;

import lombok.Value;

import java.util.List;

/** Internal checkout contract. The caller must hold the transaction until checkout completes. */
@Value
public class CartSnapshot {
    private final Long cartId;
    private final Long userId;
    private final List<Item> items;

    public CartSnapshot(Long cartId, Long userId, List<Item> items) {
        this.cartId = cartId;
        this.userId = userId;
        this.items = List.copyOf(items);
    }

    @Value
    public static class Item {
        private final Long cartItemId;
        private final Long variantId;
        private final Integer quantity;
    }
}
