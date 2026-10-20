package org.akira.laptopstore.payment.repository;

import java.util.List;
import java.util.Optional;

import org.akira.laptopstore.payment.entity.PaymentTransaction;
import org.akira.laptopstore.payment.enums.PaymentTransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByPaymentId(Long paymentId);

    Optional<PaymentTransaction> findByProviderTransactionId(Long providerTransactionId);

    boolean existsByProviderTransactionId(long providerTransactionId);

    boolean existsByPaymentIdAndStatus(Long paymentId, PaymentTransactionStatus status);

    Optional<PaymentTransaction> findTopByPaymentIdOrderByCreatedAtDesc(Long paymentId);

    Optional<PaymentTransaction> findByProviderReference(String providerReference);
}