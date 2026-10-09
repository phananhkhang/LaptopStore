package org.akira.laptopstore.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotBlank(message = "Recipient name is required")
    @Size(max = 100, message = "Recipient name must not exceed 100 characters")
    private String recipientName;

    @NotBlank(message = "Recipient phone is required")
    @Size(max = 20, message = "Recipient phone must not exceed 20 characters")
    private String recipientPhone;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String couponCode;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    private String idempotencyKey;
}
