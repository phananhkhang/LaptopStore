package org.akira.laptopstore.payment.repository;

import java.util.List;
import java.util.Optional;

import org.akira.laptopstore.payment.entity.PaymentTransaction;
import org.akira.laptopstore.payment.enums.PaymentProvider;
import org.akira.laptopstore.payment.enums.PaymentTransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

        List<PaymentTransaction> findByPaymentId(Long paymentId);

        Optional<PaymentTransaction> findByProviderTransactionId(Long providerTransactionId);

        boolean existsByProviderTransactionId(long providerTransactionId);

        boolean existsByPaymentIdAndStatus(Long paymentId, PaymentTransactionStatus status);

        Optional<PaymentTransaction> findTopByPaymentIdOrderByCreatedAtDesc(Long paymentId);

        Optional<PaymentTransaction> findByProviderReference(String providerReference);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        select pt
                        from PaymentTransaction pt
                        where pt.providerReference = :providerReference
                        """)
        Optional<PaymentTransaction> findByProviderReferenceForUpdate(
                        @Param("providerReference") String providerReference);

        Optional<PaymentTransaction> findByProviderTransactionId(
                        String providerTransactionId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        select pt
                        from PaymentTransaction pt
                        where pt.provider = :provider
                        and pt.providerReference = :providerReference
                        """)
        Optional<PaymentTransaction> findByProviderAndProviderReferenceForUpdate(
                        @Param("provider") PaymentProvider provider,
                        @Param("providerReference") String providerReference);
}