package org.akira.laptopstore.order.dto.request;

import jakarta.validation.constraints.NotNull;
import org.akira.laptopstore.order.entity.enums.OrderStatus;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Order status is required")
        OrderStatus status,

        String note
) {
}
