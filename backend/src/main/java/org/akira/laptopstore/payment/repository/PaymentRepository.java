package org.akira.laptopstore.payment.repository;

import java.util.Optional;

import org.akira.laptopstore.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from Payment p
            where p.orderId = :orderId
            """)
    Optional<Payment> findByOrderIdForUpdate(
            @Param("orderId") Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from Payment p
            where p.id = :id
            """)
    Optional<Payment> findByIdForUpdate(
            @Param("id") Long id);
}
