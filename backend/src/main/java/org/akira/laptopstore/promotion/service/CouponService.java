package org.akira.laptopstore.promotion.service;

import org.akira.laptopstore.promotion.dto.request.CreateCouponRequest;
import org.akira.laptopstore.promotion.dto.request.UpdateCouponRequest;
import org.akira.laptopstore.promotion.dto.response.CouponResponse;

import java.util.List;
import java.math.BigDecimal;
import org.akira.laptopstore.promotion.dto.response.CouponEvaluation;

public interface CouponService {

    CouponResponse createCoupon(CreateCouponRequest request);

    List<CouponResponse> getAllCoupons();

    CouponResponse getCouponById(Long id);

    CouponResponse updateCoupon(Long id, UpdateCouponRequest request);

    void deleteCoupon(Long id);

    CouponEvaluation reserveForOrder(String code, Long userId, Long orderId, BigDecimal subtotal);

    void releaseForOrder(Long orderId);
}