package org.akira.laptopstore.promotion.service.Impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.promotion.dto.response.CouponUsageResponse;
import org.akira.laptopstore.promotion.entity.CouponUsage;
import org.akira.laptopstore.promotion.enums.CouponUsageStatus;
import org.akira.laptopstore.promotion.repository.CouponUsageRepository;
import org.akira.laptopstore.promotion.service.CouponUsageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponUsageServiceImpl implements CouponUsageService {

        private final CouponUsageRepository couponUsageRepository;

        @Override
        @Transactional(readOnly = true)
        public CouponUsageResponse getById(Long id) {

                return toResponse(findUsage(id));
        }

        @Override
        @Transactional(readOnly = true)
        public List<CouponUsageResponse> getAll() {

                return couponUsageRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<CouponUsageResponse> getByCouponId(Long couponId) {

                return couponUsageRepository
                                .findByCouponId(couponId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<CouponUsageResponse> getByUserId(Long userId) {

                return couponUsageRepository
                                .findByUserId(userId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Override
        @Transactional
        public void releaseUsage(Long id) {

                CouponUsage usage = findUsage(id);

                // idempotent: đã trả lượt thì không làm gì thêm
                if (usage.getStatus() == CouponUsageStatus.RELEASED) {
                        return;
                }

                usage.setStatus(CouponUsageStatus.RELEASED);

                couponUsageRepository.save(usage);
        }

        private CouponUsage findUsage(Long id) {

                return couponUsageRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Coupon usage not found"));
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
