package org.akira.laptopstore.promotion.repository;

import org.akira.laptopstore.promotion.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CouponRepository
        extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from Coupon c
            where upper(c.code) = upper(:code)
            """)
    Optional<Coupon> findByCodeForUpdate(
            @Param("code") String code);
}