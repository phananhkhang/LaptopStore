package org.akira.laptopstore.cart.controller;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.cart.dto.request.AddCartItemRequest;
import org.akira.laptopstore.cart.dto.request.UpdateCartItemRequest;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/cart")
@RequiredArgsConstructor
public class CartController {
    // Giả lap user id hiện tại là 1L vì chưa có security
    private static final Long CURRENT_USER_ID = 1L;

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart(CURRENT_USER_ID));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(@RequestBody AddCartItemRequest request) {
        CartResponse response = cartService.addItemToCart(CURRENT_USER_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(
                cartService.updateCartItem(CURRENT_USER_ID, cartItemId, request)
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId) {
        cartService.removeCartItem(CURRENT_USER_ID, cartItemId);
        return ResponseEntity.noContent().build();
    }
}
