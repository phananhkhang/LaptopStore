package org.akira.laptopstore.promotion.service;

import org.akira.laptopstore.promotion.dto.response.CouponUsageResponse;

import java.util.List;

public interface CouponUsageService {

    /**
     * Lấy chi tiết một lần sử dụng coupon theo ID.
     */
    CouponUsageResponse getById(Long id);

    /**
     * Lấy toàn bộ lịch sử sử dụng coupon.
     */
    List<CouponUsageResponse> getAll();

    /**
     * Lấy lịch sử sử dụng của một coupon cụ thể.
     */
    List<CouponUsageResponse> getByCouponId(Long couponId);

    /**
     * Lấy lịch sử sử dụng coupon của một user.
     */
    List<CouponUsageResponse> getByUserId(Long userId);

    /**
     * Giải phóng một lượt sử dụng coupon.
     * Chuyển trạng thái CouponUsage sang RELEASED.
     * Nếu đã RELEASED thì không xử lý lại.
     */
    void releaseUsage(Long id);
}
