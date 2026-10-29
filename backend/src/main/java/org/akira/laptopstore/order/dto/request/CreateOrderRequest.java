package org.akira.laptopstore.order.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private Long addressId;

    @Size(max = 100, message = "Recipient name must not exceed 100 characters")
    private String recipientName;

    @Size(max = 20, message = "Recipient phone must not exceed 20 characters")
    private String recipientPhone;

    private String shippingAddress;

    private String paymentMethod;

    private String couponCode;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    @Size(max = 200, message = "Idempotency-Key must not exceed 200 characters")
    private String idempotencyKey;

    public CreateOrderRequest(
            String recipientName,
            String recipientPhone,
            String shippingAddress,
            String couponCode,
            String note,
            String idempotencyKey
    ) {
        this(null, recipientName, recipientPhone, shippingAddress, null, couponCode, note, idempotencyKey);
    }

    public CreateOrderRequest(Long addressId, String paymentMethod, String couponCode) {
        this(addressId, null, null, null, paymentMethod, couponCode, null, null);
    }
}
