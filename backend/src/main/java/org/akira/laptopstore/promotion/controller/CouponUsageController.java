package org.akira.laptopstore.promotion.controller;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.promotion.dto.request.CreateCouponUsageRequest;
import org.akira.laptopstore.promotion.dto.response.CouponUsageResponse;
import org.akira.laptopstore.promotion.service.CouponUsageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupon-usages")
@RequiredArgsConstructor
public class CouponUsageController {

    private final CouponUsageService couponUsageService;

    @PostMapping
    public ResponseEntity<CouponUsageResponse> create(
            @RequestBody CreateCouponUsageRequest request) {

        CouponUsageResponse response = couponUsageService.createUsage(request);

        return ResponseEntity
                .status(201)
                .body(response);
    }

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