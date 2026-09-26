package org.akira.laptopstore.payment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class PaymentResponse {

    private Long id;

    private Long orderId;

    private String method;

    private BigDecimal amount;

    private String status;

    private Instant createdAt;

    private Instant expiresAt;
}