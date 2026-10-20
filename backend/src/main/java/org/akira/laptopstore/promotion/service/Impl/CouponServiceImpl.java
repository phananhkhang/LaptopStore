package org.akira.laptopstore.promotion.service.Impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.promotion.dto.request.CreateCouponRequest;
import org.akira.laptopstore.promotion.dto.request.UpdateCouponRequest;
import org.akira.laptopstore.promotion.dto.response.CouponEvaluation;
import org.akira.laptopstore.promotion.dto.response.CouponResponse;
import org.akira.laptopstore.promotion.entity.Coupon;
import org.akira.laptopstore.promotion.entity.CouponUsage;
import org.akira.laptopstore.promotion.enums.CouponUsageStatus;
import org.akira.laptopstore.promotion.enums.DiscountType;
import org.akira.laptopstore.promotion.repository.CouponRepository;
import org.akira.laptopstore.promotion.repository.CouponUsageRepository;
import org.akira.laptopstore.promotion.service.CouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

        private final CouponRepository couponRepository;
        private final CouponUsageRepository couponUsageRepository;

        @Override
        public CouponResponse createCoupon(
                        CreateCouponRequest request) {

                if (request == null || request.getCode() == null || request.getCode().isBlank()) {
                        throw new ValidationException("Coupon code is required");
                }
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
                                .code(code)
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
                if (request == null || request.getCode() == null || request.getCode().isBlank()) {
                        throw new ValidationException("Coupon code is required");
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
                String newCode = request.getCode().trim().toUpperCase();

                // check duplicate
                if (!coupon.getCode().equalsIgnoreCase(newCode) && couponRepository.existsByCodeIgnoreCase(newCode)) {

                        throw new RuntimeException(
                                        "Coupon code already exists");
                }

                coupon.setCode(code);
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

        @Override
        @Transactional(readOnly = true)
        public CouponEvaluation evaluateForCheckout(
                        String code,
                        Long userId,
                        BigDecimal subtotal) {

                if (code == null || code.isBlank()) {
                        throw new ValidationException("Coupon code is required");
                }
                if (userId == null || userId <= 0) {
                        throw new ValidationException("Invalid user id");
                }
                if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new ValidationException("Subtotal must be greater than 0");
                }

                String normalizedCode = code.trim().toUpperCase();
                Coupon coupon = couponRepository
                                .findByCodeIgnoreCase(normalizedCode)
                                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

                validateCouponUsable(coupon);
                validateMinimumOrder(coupon, subtotal);
                validateUsageLimit(coupon, userId);

                BigDecimal discount = calculateDiscount(coupon, subtotal);
                return new CouponEvaluation(coupon.getId(), coupon.getCode(), discount);
        }

        @Override
        @Transactional(propagation = Propagation.MANDATORY)
        public CouponEvaluation reserveForOrder(
                        String code,
                        Long userId,
                        Long orderId,
                        BigDecimal subtotal) {

                if (code == null || code.isBlank()) {
                        throw new ValidationException(
                                        "Coupon code is required");
                }

                if (userId == null || userId <= 0) {
                        throw new ValidationException(
                                        "Invalid user id");
                }

                if (orderId == null || orderId <= 0) {
                        throw new ValidationException(
                                        "Invalid order id");
                }

                if (subtotal == null
                                || subtotal.compareTo(BigDecimal.ZERO) <= 0) {

                        throw new ValidationException(
                                        "Subtotal must be greater than 0");
                }

                if (couponUsageRepository.existsByOrderId(orderId)) {
                        throw new ValidationException(
                                        "Order already has a coupon");
                }

                String normalizedCode = code.trim().toUpperCase();

                Coupon coupon = couponRepository
                                .findByCodeForUpdate(normalizedCode)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Coupon not found"));

                validateCouponUsable(coupon);

                validateMinimumOrder(coupon, subtotal);

                validateUsageLimit(
                                coupon,
                                userId);

                BigDecimal discount = calculateDiscount(
                                coupon,
                                subtotal);

                CouponUsage usage = CouponUsage.builder()
                                .coupon(coupon)
                                .userId(userId)
                                .orderId(orderId)
                                .discountAmount(discount)
                                .status(
                                                CouponUsageStatus.ACTIVE)
                                .build();

                couponUsageRepository.save(usage);

                return new CouponEvaluation(
                                coupon.getId(),
                                coupon.getCode(),
                                discount);
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
                        throw new ValidationException(
                                        "Coupon is not active");
                }

                if (coupon.getStartsAt() != null
                                && now.isBefore(
                                                coupon.getStartsAt())) {

                        throw new ValidationException(
                                        "Coupon is not effective yet");
                }

                if (coupon.getEndsAt() != null
                                && !now.isBefore(
                                                coupon.getEndsAt())) {

                        throw new ValidationException(
                                        "Coupon has expired");
                }
        }

        private void validateMinimumOrder(Coupon coupon, BigDecimal subtotal) {

                if (coupon.getMinOrderAmount() != null && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                        throw new ValidationException("Order does not meet minimum amount");
                }
        }

        private void validateUsageLimit(Coupon coupon, Long userId) {

                long totalUsed = couponUsageRepository
                                .countByCouponIdAndStatus(
                                                coupon.getId(),
                                                CouponUsageStatus.ACTIVE);

                if (coupon.getUsageLimit() != null
                                && totalUsed >= coupon.getUsageLimit()) {

                        throw new ValidationException(
                                        "Coupon usage limit has been reached");
                }

                long userUsed = couponUsageRepository
                                .countByCouponIdAndUserIdAndStatus(
                                                coupon.getId(),
                                                userId,
                                                CouponUsageStatus.ACTIVE);

                if (coupon.getUsagePerUser() != null
                                && userUsed >= coupon.getUsagePerUser()) {

                        throw new ValidationException(
                                        "User has reached coupon usage limit");
                }
        }

        private BigDecimal calculateDiscount(
                        Coupon coupon,
                        BigDecimal subtotal) {

                BigDecimal discount;

                if (coupon.getDiscountType() == DiscountType.PERCENT) {

                        discount = subtotal
                                        .multiply(coupon.getDiscountValue())
                                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                } else {

                        discount = coupon.getDiscountValue();
                }

                if (coupon.getMaxDiscount() != null
                                && discount.compareTo(
                                                coupon.getMaxDiscount()) > 0) {

                        discount = coupon.getMaxDiscount();
                }

                if (discount.compareTo(subtotal) > 0) {
                        discount = subtotal;
                }

                if (discount.compareTo(
                                BigDecimal.ZERO) < 0) {

                        discount = BigDecimal.ZERO;
                }

                return discount.setScale(
                                2,
                                RoundingMode.HALF_UP);
        }

        @Override
        @Transactional(propagation = Propagation.MANDATORY)
        public void releaseForOrder(Long orderId) {

                CouponUsage usage = couponUsageRepository
                                .findByOrderId(orderId)
                                .orElse(null);

                if (usage == null) {
                        return;
                }

                if (usage.getStatus() == CouponUsageStatus.RELEASED) {
                        return;
                }

                usage.setStatus(
                                CouponUsageStatus.RELEASED);

                couponUsageRepository.save(usage);
        }
}