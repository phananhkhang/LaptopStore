package org.akira.laptopstore.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.order.dto.request.CheckoutPreviewRequest;
import org.akira.laptopstore.order.dto.request.CreateOrderRequest;
import org.akira.laptopstore.order.dto.response.CheckoutPreviewResponse;
import org.akira.laptopstore.order.dto.response.OrderPageResponse;
import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.akira.laptopstore.order.dto.response.OrderSummaryResponse;
import org.akira.laptopstore.order.service.OrderService;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Lấy danh sách đơn hàng của chính họ
    @GetMapping("/me/orders")
    public ResponseEntity<OrderPageResponse<OrderSummaryResponse>> getOwnOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(OrderPageResponse.from(orderService.getOwnOrders(userDetails.getId(), pageable)));
    }

    // Lấy thông tin một đơn hàng cụ thể của chính họ
    @GetMapping("/me/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOwnOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(orderService.getOwnOrder(userDetails.getId(), orderId));
    }

    // Lấy toàn bộ đơn hàng trong hệ thống
    @GetMapping("/staff/orders")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<OrderPageResponse<OrderSummaryResponse>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(OrderPageResponse.from(orderService.getAllOrders(pageable)));
    }

    // Xem trước thông tin checkout (địa chỉ, phương thức thanh toán, subtotal, discount, shipping, total)
    @PostMapping("/me/orders/preview")
    public ResponseEntity<CheckoutPreviewResponse> previewCheckout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody(required = false) CheckoutPreviewRequest request
    ) {
        CheckoutPreviewRequest safeRequest = request != null ? request : new CheckoutPreviewRequest();
        return ResponseEntity.ok(orderService.previewCheckout(userDetails.getId(), safeRequest));
    }

    // Tạo order từ toàn bộ các item hiện có trong giỏ hàng của user.
    @PostMapping("/me/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            request.setIdempotencyKey(idempotencyKey.trim());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(userDetails.getId(), request));
    }
}
