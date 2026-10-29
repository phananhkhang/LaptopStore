package org.akira.laptopstore.cart.service;

import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.cart.entity.CartItem;
import org.akira.laptopstore.cart.mapper.CartMapper;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CartUnavailableItemTest {
    @Test void missingCatalogEntryStillReturnsItemIdForRemoval() {
        Cart cart = Cart.builder().id(1L).userId(42L).build();
        CartItem item = CartItem.builder().id(9L).variantId(7L).quantity(2).build();
        item.setCart(cart); cart.getItems().add(item);
        var response = CartMapper.toResponse(cart, Map.of());
        var unavailable = response.getItems().getFirst();
        assertEquals(9L, unavailable.getCartItemId());
        assertFalse(unavailable.getSellable());
        assertEquals(0, unavailable.getAvailableQuantity());
        assertEquals(0, unavailable.getLineTotal().signum());
    }
}
