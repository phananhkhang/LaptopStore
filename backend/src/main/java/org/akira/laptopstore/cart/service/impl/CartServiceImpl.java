package org.akira.laptopstore.cart.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.cart.dto.request.AddCartItemRequest;
import org.akira.laptopstore.cart.dto.request.UpdateCartItemRequest;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.cart.entity.CartItem;
import org.akira.laptopstore.cart.mapper.CartMapper;
import org.akira.laptopstore.cart.repository.CartRepository;
import org.akira.laptopstore.cart.service.CartService;
import org.akira.laptopstore.catalog.entity.ProductVariant;
import org.akira.laptopstore.catalog.repository.ProductVariantRepository;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.UnauthenticatedException;
import org.akira.laptopstore.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        requireUserId(userId);
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> Cart.builder().userId(userId).build());
        return CartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(Long userId, AddCartItemRequest request) {
        requireUserId(userId);
        if (request == null || request.productVariantId() == null || request.productVariantId() <= 0) {
            throw new ValidationException("productVariantId phai lon hon 0");
        }
        int quantity = requirePositiveQuantity(request.quantity());

        ProductVariant variant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found: " + request.productVariantId()));
        if (!"ACTIVE".equalsIgnoreCase(variant.getStatus())) {
            throw new ValidationException("Product variant is not available for purchase");
        }

        cartRepository.createCartIfAbsent(userId);
        Cart cart = lockedCart(userId);

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> request.productVariantId().equals(item.getVariantId()))
                .findFirst()
                .orElse(null);
        if (existingItem == null) {
            CartItem cartItem = CartItem.builder()
                    .variantId(request.productVariantId())
                    .quantity(quantity)
                    .build();
            cartItem.setCart(cart);
            cart.getItems().add(cartItem);
        } else {
            try {
                existingItem.setQuantity(Math.addExact(existingItem.getQuantity(), quantity));
            } catch (ArithmeticException exception) {
                throw new ValidationException("quantity is too large");
            }
        }
        return CartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        requireUserId(userId);
        requireItemId(cartItemId);
        int quantity = requirePositiveQuantity(request == null ? null : request.quantity());

        Cart cart = lockedCart(userId);
        CartItem cartItem = findItem(cart, cartItemId);
        cartItem.setQuantity(quantity);

        return CartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        requireUserId(userId);
        requireItemId(cartItemId);

        Cart cart = lockedCart(userId);
        CartItem cartItem = findItem(cart, cartItemId);
        cart.getItems().remove(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        requireUserId(userId);

        Cart cart = lockedCart(userId);
        cart.getItems().clear();
    }

    // ------------------- Private Helper Methods -------------------

    private CartItem findItem(Cart cart, Long cartItemId) {
        return cart.getItems().stream()
                .filter(item -> cartItemId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
    }

    private Cart lockedCart(Long userId) {
        return cartRepository.findByUserIdWithItemsForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private void requireUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new UnauthenticatedException("Invalid authenticated user");
        }
    }

    private void requireItemId(Long cartItemId) {
        if (cartItemId == null || cartItemId <= 0) {
            throw new ValidationException("cartItemId must be greater than 0");
        }
    }

    private int requirePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ValidationException("quantity must be greater than 0");
        }
        return quantity;
    }
}
