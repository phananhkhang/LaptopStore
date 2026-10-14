package org.akira.laptopstore.payment.service.Impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.akira.laptopstore.payment.dto.request.RecordCodPaymentRequest;
import org.akira.laptopstore.payment.dto.request.StartPaymentRequest;
import org.akira.laptopstore.payment.dto.response.PaymentResponse;
import org.akira.laptopstore.payment.dto.response.PaymentSessionResponse;
import org.akira.laptopstore.payment.entity.Payment;
import org.akira.laptopstore.payment.enums.PaymentMethod;
import org.akira.laptopstore.payment.enums.PaymentProvider;
import org.akira.laptopstore.payment.enums.PaymentStatus;
import org.akira.laptopstore.payment.enums.PaymentTransactionStatus;
import org.akira.laptopstore.payment.integration.VnPayGateway;
import org.akira.laptopstore.payment.repository.PaymentRepository;
import org.akira.laptopstore.payment.repository.PaymentTransactionRepository;
import org.akira.laptopstore.payment.service.PaymentService;
import org.akira.laptopstore.payment.service.PaymentTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        private final PaymentTransactionService paymentTransactionService;
        private final PaymentTransactionRepository paymentTransactionRepository;

        private final VnPayGateway vnPayGateway;

        @Override
        @Transactional
        public PaymentResponse startPayment(Long userId, StartPaymentRequest request) {
                orderQueryService.validateOrderOwner(request.getOrderId(), userId);
                orderQueryService.validateOrderPayable(request.getOrderId());
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
                //

                return toResponse(saved);
        }

        @Override
        public PaymentResponse getById(Long userId, Long id) {

                Payment payment = paymentRepository.findById(id)
                                .orElseThrow(
                                                () -> new PaymentNotFoundException(
                                                                "Payment not found: " + id));

                orderQueryService.validateOrderOwner(payment.getOrderId(), userId);

                return toResponse(payment);
        }

        @Override
        public PaymentResponse getByOrderId(Long userId, Long orderId) {

                orderQueryService.validateOrderOwner(
                                orderId,
                                userId);

                Payment payment = paymentRepository.findByOrderId(orderId)
                                .orElseThrow(
                                                () -> new PaymentNotFoundException(
                                                                "Payment not found for order: " + orderId));

                return toResponse(payment);
        }

        @Transactional
        @Override
        public PaymentResponse recordCodPayment(RecordCodPaymentRequest request) {
                Payment payment = paymentRepository

                                .findByOrderIdForUpdate(request.getOrderId())
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
                orderQueryService.validateOrderDelivered(request.getOrderId());

                payment.setStatus(PaymentStatus.PAID);
                payment.setUpdatedAt(Instant.now());

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

        @Override
        @Transactional
        public PaymentSessionResponse createVnpaySession(
                        Long userId,
                        Long paymentId,
                        String clientIp) {

                Payment payment = paymentRepository
                                .findByIdForUpdate(paymentId)
                                .orElseThrow(
                                                () -> new PaymentNotFoundException(
                                                                "Payment not found"));

                orderQueryService.validateOrderOwner(
                                payment.getOrderId(),
                                userId);

                orderQueryService.validateOrderPayable(
                                payment.getOrderId());

                if (payment.getMethod() != PaymentMethod.VNPAY) {

                        throw new InvalidPaymentException(
                                        "Payment is not VNPAY");
                }

                if (payment.getStatus() == PaymentStatus.PAID) {

                        throw new InvalidPaymentStatusException(
                                        "Payment is already paid");
                }

                if (payment.getStatus() == PaymentStatus.CANCELLED) {

                        throw new InvalidPaymentStatusException(
                                        "Payment is cancelled");
                }

                if (paymentTransactionRepository
                                .existsByPaymentIdAndStatus(
                                                paymentId,
                                                PaymentTransactionStatus.PENDING)) {

                        throw new InvalidPaymentStatusException(
                                        "A payment attempt is already pending");
                }

                var latest = paymentTransactionRepository
                                .findTopByPaymentIdOrderByCreatedAtDesc(
                                                paymentId);

                if (latest.isPresent()
                                && latest.get().getStatus() != PaymentTransactionStatus.FAILED) {

                        throw new InvalidPaymentStatusException(
                                        "Payment cannot be retried");
                }

                String reference = UUID.randomUUID()
                                .toString()
                                .replace("-", "");

                Instant expiresAt = Instant.now()
                                .plusSeconds(15 * 60);

                paymentTransactionService
                                .createPendingTransaction(
                                                payment.getId(),
                                                PaymentProvider.VNPAY,
                                                null,
                                                reference);

                payment.setStatus(
                                PaymentStatus.PENDING);

                payment.setExpiresAt(expiresAt);

                paymentRepository.save(payment);

                String paymentUrl = vnPayGateway.createPaymentUrl(
                                payment.getAmount(),
                                reference,
                                clientIp,
                                expiresAt);

                return PaymentSessionResponse.builder()
                                .paymentId(payment.getId())
                                .paymentUrl(paymentUrl)
                                .expiresAt(expiresAt)
                                .build();
        }
}