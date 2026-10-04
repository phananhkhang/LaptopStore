package org.akira.laptopstore.order.controller;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.order.dto.response.OrderPageResponse;
import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.akira.laptopstore.order.dto.response.OrderSummaryResponse;
import org.akira.laptopstore.order.service.OrderService;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Lấy danh sách đơn hàng của chính họ
    @GetMapping("/api/v1/me/orders")
    public ResponseEntity<OrderPageResponse<OrderSummaryResponse>> getOwnOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(OrderPageResponse.from(orderService.getOwnOrders(userDetails.getId(), pageable)));
    }

    // Lấy thông tin một đơn hàng cụ thể của chính họ
    @GetMapping("/api/v1/me/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOwnOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(orderService.getOwnOrder(userDetails.getId(), orderId));
    }

    // Lấy toàn bộ đơn hàng trong hệ thống
    @GetMapping("/api/v1/staff/orders")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<OrderPageResponse<OrderSummaryResponse>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(OrderPageResponse.from(orderService.getAllOrders(pageable)));
    }
}
