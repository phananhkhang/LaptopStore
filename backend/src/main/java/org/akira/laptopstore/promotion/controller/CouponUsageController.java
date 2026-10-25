package org.akira.laptopstore.promotion.controller;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.promotion.dto.response.CouponUsageResponse;
import org.akira.laptopstore.promotion.service.CouponUsageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Tra cứu lượt dùng coupon cho ADMIN. Lượt dùng chỉ được tạo trong transaction
 * đặt hàng (CouponService.reserveForOrder), nên không có API tạo ở đây.
 */
@RestController
@RequestMapping("/api/v1/admin/coupon-usages")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CouponUsageController {

    private final CouponUsageService couponUsageService;

    @GetMapping
    public ResponseEntity<List<CouponUsageResponse>> getAll() {

        return ResponseEntity.ok(
                couponUsageService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponUsageResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                couponUsageService.getById(id));
    }

    @GetMapping("/coupon/{couponId}")
    public ResponseEntity<List<CouponUsageResponse>> getByCoupon(
            @PathVariable Long couponId) {

        return ResponseEntity.ok(
                couponUsageService
                        .getByCouponId(couponId));
    }

    @PutMapping("/{id}/release")
    public ResponseEntity<Void> release(
            @PathVariable Long id) {

        couponUsageService.releaseUsage(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
