package org.akira.laptopstore.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank(message = "Recipient name is required")
        @Size(max = 100, message = "Recipient name must not exceed 100 characters")
        String recipientName,

        @NotBlank(message = "Recipient phone is required")
        @Size(max = 20, message = "Recipient phone must not exceed 20 characters")
        String recipientPhone,

        @NotBlank(message = "Shipping address is required")
        String shippingAddress,

        String couponCode,

        @Size(max = 500, message = "Note must not exceed 500 characters")
        String note,

        String idempotencyKey
) {
}
