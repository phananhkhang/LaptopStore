package org.akira.laptopstore.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.akira.laptopstore.payment.enums.PaymentMethod;

@Getter
@Setter
public class StartPaymentRequest {

    @NotNull(message = "Order id is required")
    @Positive(message = "Order id must be greater than 0")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;
}