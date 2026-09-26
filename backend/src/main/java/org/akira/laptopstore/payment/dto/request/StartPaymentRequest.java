package org.akira.laptopstore.payment.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartPaymentRequest {

    private Long orderId;

    private String method;
}