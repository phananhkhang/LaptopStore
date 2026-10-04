package org.akira.laptopstore.cart.service;

import org.akira.laptopstore.cart.dto.request.AddCartItemRequest;
import org.akira.laptopstore.cart.dto.request.UpdateCartItemRequest;
import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.cart.entity.CartItem;
import org.akira.laptopstore.cart.repository.CartRepository;
import org.akira.laptopstore.cart.service.impl.CartServiceImpl;
import org.akira.laptopstore.catalog.entity.Product;
import org.akira.laptopstore.catalog.entity.ProductVariant;
import org.akira.laptopstore.catalog.repository.ProductVariantRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
    @Mock private CartRepository carts;
    @Mock private ProductVariantRepository productVariants;
    private CartServiceImpl service;
    private Cart cart;
    private final Map<Long, ProductVariant> catalogVariants = new HashMap<>();

    @BeforeEach
    void setUp() {
        service = new CartServiceImpl(carts, productVariants);
        cart = Cart.builder().id(10L).userId(42L).build();
        catalogVariants.put(7L, variant(7L));
        catalogVariants.put(8L, variant(8L));
        lenient().when(productVariants.findAllById(any())).thenAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            List<ProductVariant> found = new ArrayList<>();
            ids.forEach(id -> {
                ProductVariant variant = catalogVariants.get(id);
                if (variant != null) found.add(variant);
            });
            return found;
        });
    }

    @Test
    void responseContainsCurrentCatalogDetailsAndTotal() {
        addLine(9L, 7L, 2);
        when(carts.findByUserIdWithItems(42L)).thenReturn(Optional.of(cart));
        var response = service.getCart(42L);
        assertEquals(9L, response.items().getFirst().cartItemId());
        assertEquals(7L, response.items().getFirst().variantId());
        assertEquals(2, response.items().getFirst().quantity());
        assertEquals("Laptop", response.items().getFirst().productName());
        assertEquals("SKU-7", response.items().getFirst().sku());
        assertEquals("Laptop (16GB - 512GB - Silver)", response.items().getFirst().variantName());
        assertEquals(new BigDecimal("12500000"), response.items().getFirst().unitPrice());
        assertEquals(new BigDecimal("25000000"), response.totalAmount());
        assertEquals(2, response.totalItems());
    }

    @Test
    void newUserCanReadAnEmptyCartWithoutCreatingIt() {
        when(carts.findByUserIdWithItems(42L)).thenReturn(Optional.empty());
        var response = service.getCart(42L);
        assertTrue(response.items().isEmpty());
        assertEquals(0, response.totalItems());
        assertEquals(BigDecimal.ZERO, response.totalAmount());
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

    private ProductVariant variant(Long id) {
        return ProductVariant.builder().id(id).product(Product.builder().name("Laptop").build())
                .ram("16GB").storage("512GB").color("Silver").sku("SKU-" + id)
                .price(new BigDecimal("12500000")).quantity(10).status("ACTIVE").build();
    }
}
