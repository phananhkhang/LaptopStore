package org.akira.laptopstore.cart.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.cart.dto.request.AddCartItemRequest;
import org.akira.laptopstore.cart.dto.request.UpdateCartItemRequest;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.dto.response.CartSnapshot;
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
import org.springframework.transaction.annotation.Propagation;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(Long userId, AddCartItemRequest request) {
        requireUserId(userId);
        if (request == null || request.productVariantId() == null || request.productVariantId() <= 0) {
            throw new ValidationException("productVariantId must be greater than 0");
        }
        int quantity = requirePositiveQuantity(request.quantity());
        ProductVariant variant = productVariantRepository.findById(request.productVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product variant not found: " + request.productVariantId()));
        Integer stock = variant.getQuantity();
        if (stock == null || quantity > stock) {
            throw new ValidationException("Not enough stock for product variant: " + request.productVariantId());
        }
        if (!"ACTIVE".equalsIgnoreCase(variant.getStatus())
                || variant.getProduct() == null
                || !"ACTIVE".equalsIgnoreCase(variant.getProduct().getStatus())) {
            throw new ValidationException("Product variant is not available: " + request.productVariantId());
        }
        cartRepository.createCartIfAbsent(userId); // Tạo giỏ hàng cho người mới, nếu chưa có
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
                int newQuantity = Math.addExact(existingItem.getQuantity(), quantity);
                if (newQuantity > stock) {
                    throw new ValidationException("Not enough stock for product variant: " + request.productVariantId());
                }
                existingItem.setQuantity(newQuantity);
            } catch (ArithmeticException exception) {
                throw new ValidationException("quantity is too large");
            }
        }
        cart.setUpdatedAt(Instant.now());
        return toResponse(cartRepository.saveAndFlush(cart));
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        requireUserId(userId);
        requireItemId(cartItemId);
        int quantity = requirePositiveQuantity(request == null ? null : request.quantity());
        Cart cart = lockedCart(userId);
        CartItem cartItem = findItem(cart, cartItemId);
        ProductVariant variant = productVariantRepository.findById(cartItem.getVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product variant not found: " + cartItem.getVariantId()));
        if (!"ACTIVE".equalsIgnoreCase(variant.getStatus())
                || variant.getProduct() == null
                || !"ACTIVE".equalsIgnoreCase(variant.getProduct().getStatus())) {
            throw new ValidationException("Product variant is not available: " + cartItem.getVariantId());
        }
        Integer stock = variant.getQuantity();
        if (stock == null || quantity > stock) {
            throw new ValidationException("Not enough stock for product variant: " + cartItem.getVariantId());
        }
        cartItem.setQuantity(quantity);

        cart.setUpdatedAt(Instant.now());
        return toResponse(cartRepository.saveAndFlush(cart)); // bắt buộc dùng saveAndFlush để đảm bảo dữ liệu được ghi vào DB ngay lập tức, tránh lỗi khi checkout
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        requireUserId(userId);
        requireItemId(cartItemId);

        Cart cart = lockedCart(userId);
        CartItem cartItem = findItem(cart, cartItemId);
        cart.getItems().remove(cartItem);
        cart.setUpdatedAt(Instant.now());
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        requireUserId(userId);

       Cart cart = lockedCart(userId);
       cart.getItems().clear();
       cart.setUpdatedAt(Instant.now());
    }


    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public CartSnapshot lockAndGetCheckoutCart(Long userId) {
        requireUserId(userId);
        Cart cart = lockedCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new ValidationException("Cart is empty");
        }
        return new CartSnapshot(cart.getId(), userId, cart.getItems().stream()
                .map(item -> new CartSnapshot.Item(item.getId(), item.getVariantId(), item.getQuantity()))
                .toList());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void clearPurchasedItems(Long userId, Collection<Long> cartItemIds) {
        requireUserId(userId);
        if (cartItemIds == null || cartItemIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new ValidationException("cartItemIds must contain positive IDs");
        }
        if (cartItemIds.isEmpty()) {
            return;
        }
        Cart cart = lockedCart(userId);
        Set<Long> purchasedIds = Set.copyOf(cartItemIds);
        // Validate all IDs before changing the cart; an item from another user is never removed.
        purchasedIds.forEach(id -> findItem(cart, id)); // Kiem tra cac item thuoc cart hien tai, khong cho xoa bua
        cart.getItems().removeIf(item -> purchasedIds.contains(item.getId()));
        cart.setUpdatedAt(Instant.now());
    }

    // ------------------- Private Helper Methods -------------------

    private CartItem findItem(Cart cart, Long cartItemId) {
        return cart.getItems().stream()
                .filter(item -> cartItemId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
    }

    private Cart lockedCart(Long userId) {
        return cartRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private CartResponse toResponse(Cart cart) {
        Set<Long> variantIds = cart.getItems().stream()
                .map(CartItem::getVariantId)
                .collect(Collectors.toSet());
        Map<Long, ProductVariant> variantsById = productVariantRepository.findAllById(variantIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, variant -> variant));
        return CartMapper.toResponse(cart, variantsById);
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
