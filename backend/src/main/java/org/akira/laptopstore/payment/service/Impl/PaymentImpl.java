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
import org.akira.laptopstore.order.service.OrderQueryService;
import lombok.RequiredArgsConstructor;

import org.akira.laptopstore.common.exception.PaymentNotFoundException;
import org.akira.laptopstore.common.exception.PaymentAlreadyExistsException;
import org.akira.laptopstore.common.exception.InvalidPaymentException;
import org.akira.laptopstore.common.exception.InvalidPaymentStatusException;

@Service
@RequiredArgsConstructor
public class PaymentImpl implements PaymentService {

        private final PaymentRepository paymentRepository;
        private final OrderQueryService orderQueryService;

        @Override
        public PaymentResponse startPayment(StartPaymentRequest request) {
                if (paymentRepository.existsByOrderId(request.getOrderId())) {
                        throw new PaymentAlreadyExistsException(
                                        "Payment already exists for this order");
                }

                // contract to orderservice
                BigDecimal amount = orderQueryService.getOrderTotalAmount(
                                request.getOrderId());

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
                                                () -> new PaymentNotFoundException(
                                                                "Payment not found: " + id));
                return toResponse(payment);
        }

        @Override
        public PaymentResponse getByOrderId(Long orderId) {
                Payment payment = paymentRepository.findByOrderId(orderId)
                                .orElseThrow(
                                                () -> new PaymentNotFoundException(
                                                                "Payment not found for order: " + orderId));

                return toResponse(payment);
        }

        @Override
        public PaymentResponse recordCodPayment(RecordCodPaymentRequest request) {
                Payment payment = paymentRepository

                                .findByOrderId(request.getOrderId())
                                .orElseThrow(
                                                () -> new PaymentNotFoundException(
                                                                "Payment not found for order: "
                                                                                + request.getOrderId()));

                if (payment.getMethod() != PaymentMethod.COD) {

                        throw new InvalidPaymentException(
                                        "Payment method is not COD");
                }

                if (payment.getStatus() != PaymentStatus.PENDING) {

                        throw new InvalidPaymentStatusException(
                                        "Only PENDING payment can be marked as paid");
                }

                payment.setStatus(
                                PaymentStatus.PAID);

                Payment saved = paymentRepository.save(payment);

                return toResponse(saved);
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