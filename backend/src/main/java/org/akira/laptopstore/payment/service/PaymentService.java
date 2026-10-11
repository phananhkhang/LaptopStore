package org.akira.laptopstore.payment.service;

import org.akira.laptopstore.payment.dto.request.RecordCodPaymentRequest;
import org.akira.laptopstore.payment.dto.request.StartPaymentRequest;
import org.akira.laptopstore.payment.dto.response.PaymentResponse;
import org.akira.laptopstore.payment.entity.Payment;

public interface PaymentService {

    PaymentResponse startPayment(Long userId, StartPaymentRequest request);

    PaymentResponse getById(Long userId, Long id);

    PaymentResponse getByOrderId(Long userId, Long orderId);

    PaymentResponse recordCodPayment(RecordCodPaymentRequest request);
}
