package org.akira.laptopstore.promotion.service.Impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.promotion.dto.request.CreateCouponUsageRequest;
import org.akira.laptopstore.promotion.dto.response.CouponUsageResponse;
import org.akira.laptopstore.promotion.entity.Coupon;
import org.akira.laptopstore.promotion.entity.CouponUsage;
import org.akira.laptopstore.promotion.enums.CouponUsageStatus;
import org.akira.laptopstore.promotion.repository.CouponRepository;
import org.akira.laptopstore.promotion.repository.CouponUsageRepository;
import org.akira.laptopstore.promotion.service.CouponUsageService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponUsageServiceImpl implements CouponUsageService {

        private final CouponUsageRepository couponUsageRepository;
        private final CouponRepository couponRepository;

        @Override
        public CouponUsageResponse createUsage(CreateCouponUsageRequest request) {

                validateCouponUsage(request);

                Coupon coupon = couponRepository
                                .findById(request.getCouponId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Coupon not found"));

                validateUsageLimit(coupon, request.getUserId());

                CouponUsage usage = CouponUsage.builder()
                                .coupon(coupon)
                                .userId(request.getUserId())
                                .orderId(request.getOrderId())
                                .discountAmount(request.getDiscountAmount())
                                .status(CouponUsageStatus.ACTIVE)
                                .build();

                CouponUsage saved = couponUsageRepository.save(usage);

                return toResponse(saved);
        }

        @Override
        public CouponUsageResponse getById(Long id) {
                CouponUsage usage = couponUsageRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Coupon usage not found"));

                return toResponse(usage);
        }

        @Override
        public List<CouponUsageResponse> getAll() {

                return couponUsageRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Override
        public List<CouponUsageResponse> getByCouponId(Long couponId) {

                return couponUsageRepository
                                .findByCouponId(couponId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Override
        public void releaseUsage(Long id) {

                CouponUsage usage = couponUsageRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Coupon usage not found"));

                usage.setStatus(CouponUsageStatus.RELEASED);

                couponUsageRepository.save(usage);
        }

        private void validateCouponUsage(CreateCouponUsageRequest request) {

                if (request.getCouponId() == null) {
                        throw new RuntimeException(
                                        "Coupon id is required");
                }

                if (request.getUserId() == null) {
                        throw new RuntimeException(
                                        "User id is required");
                }

                if (request.getOrderId() == null) {
                        throw new RuntimeException(
                                        "Order id is required");
                }

                if (request.getDiscountAmount() == null
                                || request.getDiscountAmount()
                                                .compareTo(BigDecimal.ZERO) <= 0) {

                        throw new RuntimeException(
                                        "Discount amount must be greater than 0");
                }

                if (couponUsageRepository
                                .existsByOrderId(request.getOrderId())) {

                        throw new RuntimeException(
                                        "This order has already used a coupon");
                }
        }

        private void validateUsageLimit(Coupon coupon, Long userId) {

                long totalUsed = couponUsageRepository.countByCouponIdAndStatus(coupon.getId(),
                                CouponUsageStatus.ACTIVE);

                if (coupon.getUsageLimit() != null && totalUsed >= coupon.getUsageLimit()) {

                        throw new RuntimeException("Coupon usage limit has been reached");
                }

                long userUsed = couponUsageRepository.countByCouponIdAndUserIdAndStatus(coupon.getId(), userId,
                                CouponUsageStatus.ACTIVE);

                if (coupon.getUsagePerUser() != null && userUsed >= coupon.getUsagePerUser()) {

                        throw new RuntimeException("User has reached the usage limit");
                }
        }

        private CouponUsageResponse toResponse(
                        CouponUsage usage) {

                return CouponUsageResponse.builder()
                                .id(usage.getId())
                                .couponId(usage.getCoupon().getId())
                                .couponCode(usage.getCoupon().getCode())
                                .userId(usage.getUserId())
                                .orderId(usage.getOrderId())
                                .discountAmount(
                                                usage.getDiscountAmount())
                                .status(usage.getStatus())
                                .createdAt(usage.getCreatedAt())
                                .build();
        }
}