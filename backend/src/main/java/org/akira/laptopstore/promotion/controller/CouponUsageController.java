package org.akira.laptopstore.promotion.controller;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.promotion.dto.response.CouponUsageResponse;
import org.akira.laptopstore.promotion.service.CouponUsageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý và tra cứu lịch sử sử dụng coupon.
 *
 * CouponUsage không được tạo trực tiếp qua Controller.
 * Nó chỉ được tạo trong transaction đặt hàng
 * thông qua CouponService.reserveForOrder().
 */
@RestController
@RequestMapping("/api/v1/admin/coupon-usages")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CouponUsageController {

        private final CouponUsageService couponUsageService;

        /**
         * Lấy toàn bộ lịch sử sử dụng coupon.
         */
        @GetMapping
        public ResponseEntity<List<CouponUsageResponse>> getAll() {

                return ResponseEntity.ok(
                                couponUsageService.getAll());
        }

        /**
         * Lấy chi tiết một CouponUsage theo ID.
         */
        @GetMapping("/{id}")
        public ResponseEntity<CouponUsageResponse> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                couponUsageService.getById(id));
        }

        /**
         * Lấy toàn bộ lượt sử dụng của một coupon cụ thể.
         *
         * Dùng để ADMIN kiểm tra coupon đã được sử dụng
         * bởi những order/user nào.
         */
        @GetMapping("/coupon/{couponId}")
        public ResponseEntity<List<CouponUsageResponse>> getByCoupon(
                        @PathVariable Long couponId) {

                return ResponseEntity.ok(
                                couponUsageService.getByCouponId(couponId));
        }

        /**
         * Giải phóng một lượt sử dụng coupon.
         *
         * Chuyển CouponUsage sang RELEASED,
         * thường dùng khi cần hoàn lại lượt coupon.
         */
        @PutMapping("/{id}/release")
        public ResponseEntity<Void> release(
                        @PathVariable Long id) {

                couponUsageService.releaseUsage(id);

                return ResponseEntity
                                .noContent()
                                .build();
        }
}