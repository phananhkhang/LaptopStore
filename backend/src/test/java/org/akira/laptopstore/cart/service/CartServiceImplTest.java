package org.akira.laptopstore.cart.service;

import org.akira.laptopstore.cart.dto.request.AddCartItemRequest;
import org.akira.laptopstore.cart.dto.request.UpdateCartItemRequest;
import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.cart.entity.CartItem;
import org.akira.laptopstore.cart.repository.CartRepository;
import org.akira.laptopstore.cart.service.impl.CartServiceImpl;
import org.akira.laptopstore.catalog.dto.response.CartVariantSnapshot;
import org.akira.laptopstore.catalog.service.CatalogCartQueryService;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.UnauthenticatedException;
import org.akira.laptopstore.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
    @Mock private CartRepository carts;
    @Mock private CatalogCartQueryService productVariants;
    private CartServiceImpl service;
    private Cart cart;
    private final Map<Long, CartVariantSnapshot> catalogVariants = new HashMap<>();

    @BeforeEach
    void setUp() {
        service = new CartServiceImpl(carts, productVariants);
        cart = Cart.builder().id(10L).userId(42L).build();
        catalogVariants.put(7L, variant(7L));
        catalogVariants.put(8L, variant(8L));
        lenient().when(productVariants.getVariants(any())).thenAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            List<CartVariantSnapshot> found = new ArrayList<>();
            ids.forEach(id -> {
                CartVariantSnapshot variant = catalogVariants.get(id);
                if (variant != null) found.add(variant);
            });
            return found;
        });
        lenient().when(productVariants.getVariant(anyLong())).thenAnswer(invocation ->
                catalogVariants.get(invocation.getArgument(0)));
    }

    @Test
    void responseContainsCurrentCatalogDetailsAndTotal() {
        addLine(9L, 7L, 2);
        when(carts.findByUserIdWithItems(42L)).thenReturn(Optional.of(cart));
        var response = service.getCart(42L);
        assertEquals(9L, response.getItems().getFirst().getCartItemId());
        assertEquals(7L, response.getItems().getFirst().getVariantId());
        assertEquals(2, response.getItems().getFirst().getQuantity());
        assertEquals("Laptop", response.getItems().getFirst().getProductName());
        assertEquals("SKU-7", response.getItems().getFirst().getSku());
        assertEquals("Laptop (16GB - 512GB - Silver)", response.getItems().getFirst().getVariantName());
        assertEquals(new BigDecimal("12500000"), response.getItems().getFirst().getUnitPrice());
        assertEquals(new BigDecimal("25000000"), response.getTotalAmount());
        assertEquals(2, response.getTotalItems());
    }

    @Test
    void newUserCanReadAnEmptyCartWithoutCreatingIt() {
        when(carts.findByUserIdWithItems(42L)).thenReturn(Optional.empty());
        var response = service.getCart(42L);
        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getTotalItems());
        assertEquals(BigDecimal.ZERO, response.getTotalAmount());
        verify(carts, never()).createCartIfAbsent(any());
    }

    @Test
    void addingAnExistingVariantMergesQuantities() {
        addLine(9L, 7L, 2);
        when(carts.findByUserIdForUpdate(42L)).thenReturn(Optional.of(cart));
        when(carts.saveAndFlush(cart)).thenReturn(cart);
        var response = service.addItemToCart(42L, new AddCartItemRequest(7L, 3));
        assertEquals(1, response.getItems().size());
        assertEquals(5, response.getItems().getFirst().getQuantity());
        verify(carts).createCartIfAbsent(42L);
        verify(carts).findByUserIdForUpdate(42L);
    }

    @Test
    void updateAndRemoveOnlyItemsOwnedByTheCart() {
        addLine(9L, 7L, 2);
        when(carts.findByUserIdForUpdate(42L)).thenReturn(Optional.of(cart));
        when(carts.saveAndFlush(cart)).thenReturn(cart);
        var response = service.updateCartItem(42L, 9L, new UpdateCartItemRequest(4));
        assertEquals(4, response.getItems().getFirst().getQuantity());
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
        assertEquals(2, snapshot.getItems().size());
        assertEquals(List.of(12L), cart.getItems().stream().map(CartItem::getId).toList());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.getItems().clear());
    }

    @Test
    void invalidInputAndEmptyCheckoutAreRejected() {
        assertThrows(UnauthenticatedException.class, () -> service.getCart(null));
        assertThrows(ValidationException.class, () -> service.addItemToCart(42L, new AddCartItemRequest(7L, 0)));
        when(carts.findByUserIdForUpdate(42L)).thenReturn(Optional.of(cart));
        assertThrows(ValidationException.class, () -> service.lockAndGetCheckoutCart(42L));
    }

    @Test
    void inactiveProductOrVariantCannotBeAdded() {
        catalogVariants.put(7L, variant(7L, "INACTIVE", "ACTIVE"));
        assertThrows(ValidationException.class, () -> service.addItemToCart(42L, new AddCartItemRequest(7L, 1)));

        catalogVariants.put(7L, variant(7L, "ACTIVE", "INACTIVE"));
        assertThrows(ValidationException.class, () -> service.addItemToCart(42L, new AddCartItemRequest(7L, 1)));
    }

    private void addLine(Long id, Long variantId, int quantity) {
        cart.getItems().add(CartItem.builder().id(id).cart(cart).variantId(variantId).quantity(quantity).build());
    }

    private CartVariantSnapshot variant(Long id) {
        return variant(id, "ACTIVE", "ACTIVE");
    }

    private CartVariantSnapshot variant(Long id, String variantStatus, String productStatus) {
        return new CartVariantSnapshot(id, "Laptop", "Laptop (16GB - 512GB - Silver)", "SKU-" + id,
                new BigDecimal("12500000"), 10,
                "ACTIVE".equals(variantStatus) && "ACTIVE".equals(productStatus));
    }
}
