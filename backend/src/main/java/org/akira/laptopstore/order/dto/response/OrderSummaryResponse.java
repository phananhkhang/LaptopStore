package org.akira.laptopstore.order.dto.response;

import org.akira.laptopstore.order.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(
        Long id,
        String orderCode,
        OrderStatus status,
        Integer totalItems,
        BigDecimal totalAmount,
        String recipientName,
        Instant createdAt
) {
}
