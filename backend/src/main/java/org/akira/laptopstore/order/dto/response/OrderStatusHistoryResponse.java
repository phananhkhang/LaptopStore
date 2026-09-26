package org.akira.laptopstore.order.dto.response;

import org.akira.laptopstore.order.entity.enums.OrderStatus;

import java.time.Instant;

public record OrderStatusHistoryResponse(
        Long id,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        Instant changedAt,
        Long changedBy
) {
}
