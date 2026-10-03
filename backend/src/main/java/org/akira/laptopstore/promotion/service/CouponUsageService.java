package org.akira.laptopstore.promotion.service;

import org.akira.laptopstore.promotion.dto.request.CreateCouponUsageRequest;
import org.akira.laptopstore.promotion.dto.response.CouponUsageResponse;

import java.util.List;

public interface CouponUsageService {

    CouponUsageResponse createUsage(CreateCouponUsageRequest request);

    CouponUsageResponse getById(Long id);

    List<CouponUsageResponse> getAll();

    List<CouponUsageResponse> getByCouponId(Long couponId);

    void releaseUsage(Long id);
}