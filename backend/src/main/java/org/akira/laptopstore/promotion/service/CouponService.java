package org.akira.laptopstore.promotion.service;

import org.akira.laptopstore.promotion.dto.request.CreateCouponRequest;
import org.akira.laptopstore.promotion.dto.request.UpdateCouponRequest;
import org.akira.laptopstore.promotion.dto.response.CouponEvaluation;
import org.akira.laptopstore.promotion.dto.response.CouponResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    CouponResponse createCoupon(CreateCouponRequest request);

    List<CouponResponse> getAllCoupons();

    CouponResponse getCouponById(Long id);

    CouponResponse updateCoupon(Long id, UpdateCouponRequest request);

    CouponResponse setActive(Long id, boolean active);

    void deleteCoupon(Long id);

    CouponEvaluation evaluateForCheckout(String code, Long userId, BigDecimal subtotal);

    CouponEvaluation reserveForOrder(String code, Long userId, Long orderId, BigDecimal subtotal);

    void releaseForOrder(Long orderId);
}