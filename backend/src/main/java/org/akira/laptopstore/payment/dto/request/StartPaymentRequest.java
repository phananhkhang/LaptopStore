package org.akira.laptopstore.payment.dto.request;

import org.akira.laptopstore.payment.enums.PaymentMethod;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartPaymentRequest {

    private Long orderId;

    private PaymentMethod method;
}