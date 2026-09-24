package org.akira.laptopstore.cart.controller;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.entity.Cart;
import org.akira.laptopstore.cart.mapper.CartMapper;
import org.akira.laptopstore.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/me/cart")
    public ResponseEntity<CartResponse> getCart() {
        Cart cart = cartService.getCart();
        CartResponse cartResponse = CartMapper.toResponse(cart);
        return ResponseEntity.ok(cartResponse);
    }
}
