package org.akira.laptopstore.promotion.service.Impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.promotion.dto.request.CreateCouponRequest;
import org.akira.laptopstore.promotion.dto.request.UpdateCouponRequest;
import org.akira.laptopstore.promotion.dto.response.CouponResponse;
import org.akira.laptopstore.promotion.entity.Coupon;
import org.akira.laptopstore.promotion.enums.DiscountType;
import org.akira.laptopstore.promotion.repository.CouponRepository;
import org.akira.laptopstore.promotion.service.CouponService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

        private final CouponRepository couponRepository;

        @Override
        public CouponResponse createCoupon(
                        CreateCouponRequest request) {

                if (couponRepository.existsByCodeIgnoreCase(request.getCode())) {
                        throw new RuntimeException("Coupon code already exists");
                }

                String code = request.getCode().trim().toUpperCase();
                validateCoupon(
                                request.getDiscountType(),
                                request.getDiscountValue(),
                                request.getMaxDiscount(),
                                request.getMinOrderAmount(),
                                request.getUsageLimit(),
                                request.getUsagePerUser(),
                                request.getStartsAt(),
                                request.getEndsAt(),
                                code);

                Coupon coupon = Coupon.builder()
                                .code(request.getCode())
                                .discountType(request.getDiscountType())
                                .discountValue(request.getDiscountValue())
                                .maxDiscount(request.getMaxDiscount())
                                .minOrderAmount(request.getMinOrderAmount())
                                .usageLimit(request.getUsageLimit())
                                .usagePerUser(request.getUsagePerUser())
                                .startsAt(request.getStartsAt())
                                .endsAt(request.getEndsAt())
                                .active(true)
                                .build();

                Coupon savedCoupon = couponRepository.save(coupon);

                return toResponse(savedCoupon);
        }

        @Override
        public List<CouponResponse> getAllCoupons() {

                return couponRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Override
        public CouponResponse getCouponById(Long id) {

                Coupon coupon = couponRepository.findById(id)
                                .orElseThrow(
                                                () -> new RuntimeException("Coupon not found"));

                return toResponse(coupon);
        }

        @Override
        public CouponResponse updateCoupon(
                        Long id,
                        UpdateCouponRequest request) {

                Coupon coupon = couponRepository.findById(id)
                                .orElseThrow(
                                                () -> new RuntimeException("Coupon not found"));
                String code = request.getCode().trim().toUpperCase();
                validateCoupon(
                                request.getDiscountType(),
                                request.getDiscountValue(),
                                request.getMaxDiscount(),
                                request.getMinOrderAmount(),
                                request.getUsageLimit(),
                                request.getUsagePerUser(),
                                request.getStartsAt(),
                                request.getEndsAt(),
                                code);
                String newCode = request.getCode().trim().toUpperCase();

                // check duplicate
                if (!coupon.getCode().equalsIgnoreCase(newCode) && couponRepository.existsByCodeIgnoreCase(newCode)) {

                        throw new RuntimeException(
                                        "Coupon code already exists");
                }

                coupon.setCode(request.getCode());
                coupon.setDiscountType(request.getDiscountType());
                coupon.setDiscountValue(request.getDiscountValue());
                coupon.setMaxDiscount(request.getMaxDiscount());
                coupon.setMinOrderAmount(request.getMinOrderAmount());
                coupon.setUsageLimit(request.getUsageLimit());
                coupon.setUsagePerUser(request.getUsagePerUser());
                coupon.setStartsAt(request.getStartsAt());
                coupon.setEndsAt(request.getEndsAt());
                coupon.setActive(request.isActive());

                Coupon savedCoupon = couponRepository.save(coupon);

                return toResponse(savedCoupon);
        }

        @Override
        public void deleteCoupon(Long id) {

                Coupon coupon = couponRepository.findById(id)
                                .orElseThrow(
                                                () -> new RuntimeException("Coupon not found"));

                couponRepository.delete(coupon);
        }

        private CouponResponse toResponse(Coupon coupon) {

                return CouponResponse.builder()
                                .id(coupon.getId())
                                .code(coupon.getCode())
                                .discountType(coupon.getDiscountType())
                                .discountValue(coupon.getDiscountValue())
                                .maxDiscount(coupon.getMaxDiscount())
                                .minOrderAmount(coupon.getMinOrderAmount())
                                .usageLimit(coupon.getUsageLimit())
                                .usagePerUser(coupon.getUsagePerUser())
                                .startsAt(coupon.getStartsAt())
                                .endsAt(coupon.getEndsAt())
                                .active(coupon.isActive())
                                .build();
        }

        private void validateCoupon(
                        DiscountType discountType,
                        BigDecimal discountValue,
                        BigDecimal maxDiscount,
                        BigDecimal minOrderAmount,
                        Integer usageLimit,
                        Integer usagePerUser,
                        Instant startsAt,
                        Instant endsAt,
                        String code) {

                // 1. Loại giảm giá bắt buộc phải có
                if (discountType == null) {
                        throw new RuntimeException(
                                        "Discount type is required");
                }

                // 2. Giá trị giảm bắt buộc > 0
                if (discountValue == null
                                || discountValue.compareTo(BigDecimal.ZERO) <= 0) {

                        throw new RuntimeException(
                                        "Discount value must be greater than 0");
                }

                // 3. Nếu giảm theo phần trăm thì không được vượt 100%
                if (discountType == DiscountType.PERCENT
                                && discountValue.compareTo(
                                                BigDecimal.valueOf(100)) > 0) {

                        throw new RuntimeException(
                                        "Percent discount cannot be greater than 100");
                }

                // 4. maxDiscount không được âm
                if (maxDiscount != null
                                && maxDiscount.compareTo(BigDecimal.ZERO) < 0) {

                        throw new RuntimeException(
                                        "Max discount cannot be negative");
                }

                // 5. minOrderAmount không được âm
                if (minOrderAmount != null
                                && minOrderAmount.compareTo(BigDecimal.ZERO) < 0) {

                        throw new RuntimeException(
                                        "Minimum order amount cannot be negative");
                }

                // 6. usageLimit phải > 0 nếu được khai báo
                if (usageLimit != null
                                && usageLimit <= 0) {

                        throw new RuntimeException(
                                        "Usage limit must be greater than 0");
                }

                // 7. usagePerUser phải > 0 nếu được khai báo
                if (usagePerUser != null
                                && usagePerUser <= 0) {

                        throw new RuntimeException(
                                        "Usage per user must be greater than 0");
                }

                // 8. usagePerUser không nên lớn hơn tổng usageLimit
                if (usageLimit != null
                                && usagePerUser != null
                                && usagePerUser > usageLimit) {

                        throw new RuntimeException(
                                        "Usage per user cannot be greater than usage limit");
                }

                // 9. Thời gian bắt đầu phải có trước thời gian kết thúc
                if (startsAt != null
                                && endsAt != null
                                && !endsAt.isAfter(startsAt)) {

                        throw new RuntimeException(
                                        "End time must be after start time");
                }
                // 10. valide Code null/format/blank
                if (code == null || code.isBlank()) {
                        throw new RuntimeException("Coupon code is required");
                }

                if (!code.matches("^[A-Z0-9_-]{3,30}$")) {
                        throw new RuntimeException(
                                        "Coupon code must be 3-30 characters and contain only A-Z, 0-9, _ or -");
                }
        }

        private void validateCouponUsable(Coupon coupon) {

                Instant now = Instant.now();

                if (!coupon.isActive()) {
                        throw new RuntimeException(
                                        "Coupon is not active");
                }

                if (coupon.getStartsAt() != null
                                && now.isBefore(coupon.getStartsAt())) {

                        throw new RuntimeException(
                                        "Coupon is not effective yet");
                }

                if (coupon.getEndsAt() != null
                                && now.isAfter(coupon.getEndsAt())) {

                        throw new RuntimeException(
                                        "Coupon has expired");
                }
        }
}