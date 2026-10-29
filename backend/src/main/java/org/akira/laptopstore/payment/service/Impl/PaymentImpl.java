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
        private final org.akira.laptopstore.order.service.OrderPaymentService orderPaymentService;

        @Override
        @Transactional
        public PaymentResponse startPayment(Long userId, StartPaymentRequest request) {
                BigDecimal amount = orderPaymentService.lockPayableOrder(request.getOrderId(), userId,
                        org.akira.laptopstore.order.entity.enums.CheckoutPaymentMethod.valueOf(request.getMethod().name()));
                Payment existing = paymentRepository.findByOrderId(request.getOrderId()).orElse(null);
                if (existing != null) {
                    if (existing.getMethod() != request.getMethod()) throw new InvalidPaymentException("Payment method mismatch");
                    return toResponse(existing);
                }

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

                if (payment.getStatus() == PaymentStatus.PAID) return toResponse(payment);
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

                orderPaymentService.lockPayableOrder(payment.getOrderId(), userId,
                        org.akira.laptopstore.order.entity.enums.CheckoutPaymentMethod.VNPAY);

                if (payment.getMethod() != PaymentMethod.VNPAY) {

                        throw new InvalidPaymentException(
                                        "Payment is not VNPAY");
                }

                if (payment.getStatus() == PaymentStatus.PAID) {

                        throw new InvalidPaymentStatusException(
                                        "Payment is already paid");
                }

                if (payment.getStatus() == PaymentStatus.CANCELLED || payment.getStatus() == PaymentStatus.REQUIRES_RECONCILIATION) {

                        throw new InvalidPaymentStatusException(
                                        "Payment is cancelled");
                }

                if (paymentTransactionRepository
                                .existsByPaymentIdAndStatus(
                                                paymentId,
                                                PaymentTransactionStatus.PENDING)) {

                        var pending = paymentTransactionRepository.findTopByPaymentIdOrderByCreatedAtDesc(paymentId)
                                .orElseThrow(() -> new InvalidPaymentStatusException("Pending attempt unavailable"));
                        if (payment.getExpiresAt() != null && payment.getExpiresAt().isAfter(Instant.now())) {
                            return PaymentSessionResponse.builder().paymentId(paymentId).expiresAt(payment.getExpiresAt())
                                    .paymentUrl(vnPayGateway.createPaymentUrl(payment.getAmount(), pending.getProviderReference(), clientIp, payment.getExpiresAt())).build();
                        }
                        throw new InvalidPaymentStatusException("Phiên thanh toán đã hết hạn; vui lòng kiểm tra đơn hàng");
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