package org.akira.laptopstore.payment.service.Impl;

import java.math.BigDecimal;

import org.akira.laptopstore.payment.dto.request.RecordCodPaymentRequest;
import org.akira.laptopstore.payment.dto.request.StartPaymentRequest;
import org.akira.laptopstore.payment.dto.response.PaymentResponse;
import org.akira.laptopstore.payment.entity.Payment;
import org.akira.laptopstore.payment.enums.PaymentMethod;
import org.akira.laptopstore.payment.enums.PaymentStatus;
import org.akira.laptopstore.payment.repository.PaymentRepository;
import org.akira.laptopstore.payment.service.PaymentService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentResponse startPayment(StartPaymentRequest request) {
        validateStartPayment(request);
        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            throw new RuntimeException(
                    "Payment already exists for this order");
        }

        // hard-code doi orderservice
        BigDecimal amount = BigDecimal.valueOf(1000000);

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .method(request.getMethod())
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();

        Payment saved = paymentRepository.save(payment);

        return toResponse(saved);
    }

    @Override
    public PaymentResponse getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Payment not found"));
        return toResponse(payment);
    }

    @Override
    public PaymentResponse getByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Payment not found"));

        return toResponse(payment);
    }

    @Override
    public PaymentResponse recordCodPayment(RecordCodPaymentRequest request) {
        Payment payment = paymentRepository
                .findByOrderId(request.getOrderId())
                .orElseThrow(
                        () -> new RuntimeException(
                                "Payment not found"));

        if (payment.getMethod() != PaymentMethod.COD) {

            throw new RuntimeException(
                    "Payment method is not COD");
        }

        if (payment.getStatus() == PaymentStatus.PAID) {

            throw new RuntimeException(
                    "Payment is already paid");
        }

        payment.setStatus(
                PaymentStatus.PAID);

        Payment saved = paymentRepository.save(payment);

        return toResponse(saved);
    }

    private void validateStartPayment(StartPaymentRequest request) {
        if (request.getOrderId() == null) {
            throw new RuntimeException(
                    "Order id is required");
        }

        if (request.getMethod() == null) {
            throw new RuntimeException(
                    "Payment method is required");
        }
    }

    public PaymentResponse toResponse(Payment payment) {

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .method(payment.getMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .expiresAt(payment.getExpiresAt())
                .build();
    }

}