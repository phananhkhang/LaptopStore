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
public class CheckoutPreviewRequest {
    private Long addressId;

    @Size(max = 100, message = "Recipient name must not exceed 100 characters")
    private String recipientName;

    @Size(max = 20, message = "Recipient phone must not exceed 20 characters")
    private String recipientPhone;

    private String shippingAddress;

    private String paymentMethod;

    private String couponCode;
}
