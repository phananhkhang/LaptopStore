package org.akira.laptopstore.payment.service;

import org.akira.laptopstore.payment.dto.request.RecordCodPaymentRequest;
import org.akira.laptopstore.payment.dto.request.StartPaymentRequest;
import org.akira.laptopstore.payment.dto.response.PaymentResponse;
import org.akira.laptopstore.payment.entity.Payment;

public interface PaymentService {

    PaymentResponse startPayment(StartPaymentRequest request);

    PaymentResponse getById(Long id);

    PaymentResponse getByOrderId(Long orderId);

    PaymentResponse recordCodPayment(RecordCodPaymentRequest request);
}
