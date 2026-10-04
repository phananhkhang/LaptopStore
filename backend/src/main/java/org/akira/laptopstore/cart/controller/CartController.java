package org.akira.laptopstore.cart.controller;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.cart.dto.request.AddCartItemRequest;
import org.akira.laptopstore.cart.dto.request.UpdateCartItemRequest;
import org.akira.laptopstore.cart.dto.response.CartResponse;
import org.akira.laptopstore.cart.service.CartService;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddCartItemRequest request
    ) {
        CartResponse response = cartService.addItemToCart(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(
                cartService.updateCartItem(userDetails.getId(), cartItemId, request)
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId
    ) {
        cartService.removeCartItem(userDetails.getId(), cartItemId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/items/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.clearCart(userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
