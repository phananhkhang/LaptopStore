package org.akira.laptopstore.payment.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import org.akira.laptopstore.payment.enums.PaymentProvider;
import org.akira.laptopstore.payment.enums.PaymentTransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionResponse {

    private Long id;

    private Long paymentId;

    private PaymentProvider provider;

    private Long providerTransactionId;

    private String providerReference;

    private BigDecimal amount;

    private PaymentTransactionStatus status;

    private Instant createdAt;
}
