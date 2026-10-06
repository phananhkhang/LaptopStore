package org.akira.laptopstore.payment.repository;

import java.util.List;
import java.util.Optional;

import org.akira.laptopstore.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByPaymentId(Long paymentId);

    Optional<PaymentTransaction> findByProviderTransactionId(String providerTransactionId);

    boolean existsByProviderTransactionId(String providerTransactionId);
}