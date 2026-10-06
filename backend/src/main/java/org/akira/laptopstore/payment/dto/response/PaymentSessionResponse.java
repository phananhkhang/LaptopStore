package org.akira.laptopstore.payment.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentSessionResponse {

    private Long paymentId;

    private String paymentUrl;

    private Instant expiresAt;
}