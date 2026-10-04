package org.akira.laptopstore.cart.service;

import org.akira.laptopstore.cart.dto.request.AddCartItemRequest;
import org.akira.laptopstore.cart.dto.request.UpdateCartItemRequest;
import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.cart.entity.CartItem;
import org.akira.laptopstore.cart.repository.CartRepository;
import org.akira.laptopstore.cart.service.impl.CartServiceImpl;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.UnauthenticatedException;
import org.akira.laptopstore.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
    @Mock private CartRepository carts;
    private CartServiceImpl service;
    private Cart cart;

    @BeforeEach
    void setUp() {
        service = new CartServiceImpl(carts);
        cart = Cart.builder().id(10L).userId(42L).build();
    }

    @Test
    void responseContainsOnlyCartOwnedDataUntilCatalogContractExists() {
        addLine(9L, 7L, 2);
        when(carts.findByUserIdWithItems(42L)).thenReturn(Optional.of(cart));
        var response = service.getCart(42L);
        assertEquals(9L, response.items().getFirst().cartItemId());
        assertEquals(7L, response.items().getFirst().variantId());
        assertEquals(2, response.items().getFirst().quantity());
        assertNull(response.items().getFirst().productName());
        assertNull(response.items().getFirst().unitPrice());
        assertNull(response.totalAmount());
        assertEquals(2, response.totalItems());
    }

    @Test
    void newUserCanReadAnEmptyCartWithoutCreatingIt() {
        when(carts.findByUserIdWithItems(42L)).thenReturn(Optional.empty());
        var response = service.getCart(42L);
        assertTrue(response.items().isEmpty());
        assertEquals(0, response.totalItems());
        assertNull(response.totalAmount());
        verify(carts, never()).createCartIfAbsent(any());
    }

    @Test
    void addingAnExistingVariantMergesQuantities() {
        addLine(9L, 7L, 2);
        when(carts.findByUserIdForUpdate(42L)).thenReturn(Optional.of(cart));
        when(carts.saveAndFlush(cart)).thenReturn(cart);
        var response = service.addItemToCart(42L, new AddCartItemRequest(7L, 3));
        assertEquals(1, response.items().size());
        assertEquals(5, response.items().getFirst().quantity());
        verify(carts).createCartIfAbsent(42L);
        verify(carts).findByUserIdForUpdate(42L);
    }

    @Test
    void updateAndRemoveOnlyItemsOwnedByTheCart() {
        addLine(9L, 7L, 2);
        when(carts.findByUserIdForUpdate(42L)).thenReturn(Optional.of(cart));
        when(carts.saveAndFlush(cart)).thenReturn(cart);
        var response = service.updateCartItem(42L, 9L, new UpdateCartItemRequest(4));
        assertEquals(4, response.items().getFirst().quantity());
        assertThrows(ResourceNotFoundException.class, () -> service.removeCartItem(42L, 999L));
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void checkoutSnapshotAndCleanupAreScopedToSelectedItems() {
        addLine(9L, 7L, 2);
        addLine(12L, 8L, 1);
        when(carts.findByUserIdForUpdate(42L)).thenReturn(Optional.of(cart));
        var snapshot = service.lockAndGetCheckoutCart(42L);
        service.clearPurchasedItems(42L, List.of(9L));
        assertEquals(2, snapshot.items().size());
        assertEquals(List.of(12L), cart.getItems().stream().map(CartItem::getId).toList());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.items().clear());
    }

    @Test
    void invalidInputAndEmptyCheckoutAreRejected() {
        assertThrows(UnauthenticatedException.class, () -> service.getCart(null));
        assertThrows(ValidationException.class, () -> service.addItemToCart(42L, new AddCartItemRequest(7L, 0)));
        when(carts.findByUserIdForUpdate(42L)).thenReturn(Optional.of(cart));
        assertThrows(ValidationException.class, () -> service.lockAndGetCheckoutCart(42L));
    }

    private void addLine(Long id, Long variantId, int quantity) {
        cart.getItems().add(CartItem.builder().id(id).cart(cart).variantId(variantId).quantity(quantity).build());
    }
}
