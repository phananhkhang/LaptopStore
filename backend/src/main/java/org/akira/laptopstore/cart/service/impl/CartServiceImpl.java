package org.akira.laptopstore.cart.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.cart.repository.CartRepository;
import org.akira.laptopstore.cart.service.CartService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    @Override
    public Cart getCart() {
        Long currentUserId = 1L;
        return cartRepository.findByUserId(currentUserId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(currentUserId)
                            .build();
                    return cartRepository.save(newCart);
                });
    }
}
