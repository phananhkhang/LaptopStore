package org.akira.laptopstore.payment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

import org.akira.laptopstore.payment.enums.PaymentMethod;
import org.akira.laptopstore.payment.enums.PaymentStatus;

@Getter
@Builder
public class PaymentResponse {

    private Long id;

    private Long orderId;

    private PaymentMethod method;

    private BigDecimal amount;

    private PaymentStatus status;

    private Instant createdAt;

    private Instant expiresAt;
}