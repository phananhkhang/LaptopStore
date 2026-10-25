package org.akira.laptopstore.promotion.service;

import org.akira.laptopstore.promotion.dto.request.CreateCouponRequest;
import org.akira.laptopstore.promotion.dto.request.UpdateCouponRequest;
import org.akira.laptopstore.promotion.dto.response.CouponEvaluation;
import org.akira.laptopstore.promotion.dto.response.CouponResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    /**
     * Tạo coupon mới.
     * Kiểm tra code không trùng và validate các điều kiện
     * như loại giảm giá, giá trị giảm, thời gian và giới hạn sử dụng.
     */
    CouponResponse createCoupon(CreateCouponRequest request);

    /**
     * Lấy danh sách tất cả coupon.
     */
    List<CouponResponse> getAllCoupons();

    /**
     * Lấy chi tiết coupon theo ID.
     */
    CouponResponse getCouponById(Long id);

    /**
     * Cập nhật thông tin coupon.
     * Kiểm tra lại tính hợp lệ và tránh trùng coupon code.
     */
    CouponResponse updateCoupon(
            Long id,
            UpdateCouponRequest request);

    /**
     * Bật hoặc tắt coupon.
     * active = true: cho phép sử dụng.
     * active = false: ngừng áp dụng coupon.
     */
    CouponResponse setActive(Long id, boolean active);

    /**
     * Xóa coupon.
     * Nếu coupon chưa từng được sử dụng thì xóa khỏi database.
     * Nếu đã có CouponUsage thì chỉ vô hiệu hóa để bảo toàn lịch sử.
     */
    void deleteCoupon(Long id);

    /**
     * Kiểm tra coupon có thể áp dụng cho checkout hay không
     * và tính số tiền được giảm.
     *
     * Method này chỉ kiểm tra và tính toán,
     * KHÔNG chiếm lượt sử dụng coupon.
     */
    CouponEvaluation evaluateForCheckout(
            String code,
            Long userId,
            BigDecimal subtotal);

    /**
     * Chính thức giữ một lượt sử dụng coupon cho đơn hàng.
     *
     * Kiểm tra lại điều kiện coupon và tạo CouponUsage = ACTIVE.
     * Được gọi trong transaction tạo đơn để tránh vượt usage limit.
     */
    CouponEvaluation reserveForOrder(
            String code,
            Long userId,
            Long orderId,
            BigDecimal subtotal);

    /**
     * Trả lại lượt sử dụng coupon khi đơn hàng bị hủy.
     * CouponUsage của order chuyển từ ACTIVE sang RELEASED.
     */
    void releaseForOrder(Long orderId);
}