package org.akira.laptopstore.promotion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.promotion.dto.request.CreateCouponRequest;
import org.akira.laptopstore.promotion.dto.request.UpdateCouponActiveRequest;
import org.akira.laptopstore.promotion.dto.request.UpdateCouponRequest;
import org.akira.laptopstore.promotion.dto.response.CouponResponse;
import org.akira.laptopstore.promotion.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản trị coupon.
 *
 * Toàn bộ API trong controller này chỉ ADMIN được truy cập.
 */
@RestController
@RequestMapping("/api/v1/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCouponController {

        private final CouponService couponService;

        /**
         * Tạo coupon mới.
         *
         * Request được validate trước khi chuyển sang Service
         * xử lý business rule và lưu database.
         */
        @PostMapping
        public ResponseEntity<CouponResponse> create(
                        @Valid @RequestBody CreateCouponRequest request) {

                CouponResponse response = couponService.createCoupon(request);

                return ResponseEntity
                                .status(201)
                                .body(response);
        }

        /**
         * Lấy toàn bộ coupon để ADMIN quản lý.
         */
        @GetMapping
        public ResponseEntity<List<CouponResponse>> getAll() {

                return ResponseEntity.ok(
                                couponService.getAllCoupons());
        }

        /**
         * Lấy chi tiết một coupon theo ID.
         */
        @GetMapping("/{id}")
        public ResponseEntity<CouponResponse> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                couponService.getCouponById(id));
        }

        /**
         * Cập nhật thông tin coupon.
         *
         * Ví dụ: code, discountValue, thời gian áp dụng,
         * minOrderValue, usageLimit...
         */
        @PutMapping("/{id}")
        public ResponseEntity<CouponResponse> update(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateCouponRequest request) {

                return ResponseEntity.ok(
                                couponService.updateCoupon(id, request));
        }

        /**
         * Bật hoặc tắt trạng thái hoạt động của coupon.
         *
         * Không xóa dữ liệu coupon,
         * chỉ thay đổi khả năng được áp dụng khi checkout.
         */
        @PatchMapping("/{id}/active")
        public ResponseEntity<CouponResponse> setActive(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateCouponActiveRequest request) {

                return ResponseEntity.ok(
                                couponService.setActive(
                                                id,
                                                request.getActive()));
        }

        /**
         * Xóa coupon.
         *
         * Service quyết định xóa vật lý hoặc chỉ vô hiệu hóa
         * tùy theo coupon đã có lịch sử sử dụng hay chưa.
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
                        @PathVariable Long id) {

                couponService.deleteCoupon(id);

                return ResponseEntity.noContent().build();
        }
}