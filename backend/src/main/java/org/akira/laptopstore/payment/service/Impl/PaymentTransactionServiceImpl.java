package org.akira.laptopstore.payment.service.Impl;

import java.time.Instant;
import java.util.List;

import org.akira.laptopstore.order.service.OrderQueryService;
import org.akira.laptopstore.payment.dto.response.PaymentTransactionResponse;
import org.akira.laptopstore.payment.entity.Payment;
import org.akira.laptopstore.payment.entity.PaymentTransaction;
import org.akira.laptopstore.payment.enums.PaymentProvider;
import org.akira.laptopstore.payment.enums.PaymentStatus;
import org.akira.laptopstore.payment.enums.PaymentTransactionStatus;
import org.akira.laptopstore.payment.repository.PaymentRepository;
import org.akira.laptopstore.payment.repository.PaymentTransactionRepository;
import org.akira.laptopstore.payment.service.PaymentTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentTransactionServiceImpl
                implements PaymentTransactionService {

        private final PaymentTransactionRepository paymentTransactionRepository;
        private final PaymentRepository paymentRepository;
        private final OrderQueryService orderQueryService;

        @Override
        @Transactional(readOnly = true)
        public PaymentTransactionResponse getById(Long userId, Long id) {

                PaymentTransaction transaction = paymentTransactionRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Payment transaction not found: " + id));

                Long orderId = transaction.getPayment().getOrderId();

                orderQueryService.validateOrderOwner(orderId, userId);

                return toResponse(transaction);
        }

        @Override
        @Transactional(readOnly = true)
        public List<PaymentTransactionResponse> getByPaymentId(Long userId, Long paymentId) {

                Payment payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Payment not found: " + paymentId));

                orderQueryService.validateOrderOwner(
                                payment.getOrderId(),
                                userId);

                return paymentTransactionRepository
                                .findByPaymentId(paymentId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Override
        @Transactional
        public PaymentTransactionResponse markSuccess(Long transactionId) {

                PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Payment transaction not found: "
                                                                + transactionId));

                if (transaction.getStatus() == PaymentTransactionStatus.SUCCESS) {

                        return toResponse(transaction);
                }

                if (transaction.getStatus() == PaymentTransactionStatus.FAILED) {

                        throw new RuntimeException(
                                        "Failed transaction cannot be changed to success");
                }

                transaction.setStatus(
                                PaymentTransactionStatus.SUCCESS);

                Payment payment = transaction.getPayment();

                payment.setStatus(PaymentStatus.PAID);
                payment.setUpdatedAt(Instant.now());

                paymentRepository.save(payment);

                PaymentTransaction saved = paymentTransactionRepository.save(transaction);

                return toResponse(saved);
        }

        @Override
        @Transactional
        public PaymentTransactionResponse markFailed(Long transactionId) {

                PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Payment transaction not found: "
                                                                + transactionId));

                if (transaction.getStatus() == PaymentTransactionStatus.SUCCESS) {

                        throw new RuntimeException(
                                        "Successful transaction cannot be changed to failed");
                }

                if (transaction.getStatus() == PaymentTransactionStatus.FAILED) {

                        return toResponse(transaction);
                }

                transaction.setStatus(
                                PaymentTransactionStatus.FAILED);
                Payment payment = transaction.getPayment();

                if (payment.getStatus() != PaymentStatus.PAID) {

                        payment.setStatus(
                                        PaymentStatus.FAILED);

                        paymentRepository.save(payment);
                }

                PaymentTransaction saved = paymentTransactionRepository.save(transaction);

                return toResponse(saved);
        }

        @Override
        @Transactional
        public PaymentTransactionResponse createPendingTransaction(Long paymentId, PaymentProvider provider,
                        Long providerTransactionId, String providerReference) {

                Payment payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Payment not found: "
                                                                + paymentId));

                PaymentTransaction transaction = PaymentTransaction.builder().payment(payment).provider(provider)
                                .providerTransactionId(providerTransactionId).providerReference(providerReference)
                                .amount(payment.getAmount()).status(PaymentTransactionStatus.PENDING)
                                .createdAt(Instant.now()).build();

                PaymentTransaction saved = paymentTransactionRepository.save(transaction);

                return toResponse(saved);
        }

        private PaymentTransactionResponse toResponse(
                        PaymentTransaction transaction) {

                return PaymentTransactionResponse.builder()
                                .id(transaction.getId())
                                .paymentId(transaction.getPayment().getId())
                                .provider(transaction.getProvider())
                                .providerTransactionId(
                                                transaction.getProviderTransactionId())
                                .providerReference(
                                                transaction.getProviderReference())
                                .amount(transaction.getAmount())
                                .status(transaction.getStatus())
                                .createdAt(transaction.getCreatedAt())
                                .build();
        }
}