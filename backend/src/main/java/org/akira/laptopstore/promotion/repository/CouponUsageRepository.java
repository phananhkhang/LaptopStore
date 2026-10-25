package org.akira.laptopstore.promotion.repository;

import org.akira.laptopstore.promotion.entity.CouponUsage;
import org.akira.laptopstore.promotion.enums.CouponUsageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponUsageRepository
                extends JpaRepository<CouponUsage, Long> {

        //usageLimit
        long countByCouponIdAndStatus(
                        Long couponId,
                        CouponUsageStatus status);
        //usagePerUser
        long countByCouponIdAndUserIdAndStatus(
                        Long couponId,
                        Long userId,
                        CouponUsageStatus status);

        boolean existsByOrderId(Long orderId);

        boolean existsByCouponId(Long couponId);

        Optional<CouponUsage> findByOrderId(Long orderId);

        List<CouponUsage> findByCouponId(Long couponId);

        List<CouponUsage> findByUserId(Long userId);
}