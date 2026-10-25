package org.akira.laptopstore.promotion.service;

import org.akira.laptopstore.promotion.dto.response.CouponUsageResponse;

import java.util.List;

public interface CouponUsageService {

    CouponUsageResponse getById(Long id);

    List<CouponUsageResponse> getAll();

    List<CouponUsageResponse> getByCouponId(Long couponId);

    List<CouponUsageResponse> getByUserId(Long userId);

    void releaseUsage(Long id);
}
