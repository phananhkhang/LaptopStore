package org.akira.laptopstore.payment.service;

import java.util.List;

import org.akira.laptopstore.payment.dto.response.PaymentTransactionResponse;
import org.akira.laptopstore.payment.entity.Payment;
import org.akira.laptopstore.payment.enums.PaymentProvider;

public interface PaymentTransactionService {

    PaymentTransactionResponse getById(Long id);

    List<PaymentTransactionResponse> getByPaymentId(Long paymentId);

    PaymentTransactionResponse markSuccess(Long transactionId);

    PaymentTransactionResponse markFailed(Long transactionId);

    PaymentTransactionResponse createPendingTransaction(Long paymentId, PaymentProvider provider,
            Long providerTransactionId, String providerReference);
}