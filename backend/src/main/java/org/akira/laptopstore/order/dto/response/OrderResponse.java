package org.akira.laptopstore.order.dto.response;

import org.akira.laptopstore.order.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String orderCode,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        String recipientName,
        String recipientPhone,
        String shippingAddress,
        String couponCodeSnapshot,
        List<OrderItemResponse> items,
        List<OrderStatusHistoryResponse> statusHistories,
        Instant createdAt,
        Instant updatedAt
) {
}
