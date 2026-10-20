package org.akira.laptopstore.promotion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.promotion.dto.request.CreateCouponRequest;
import org.akira.laptopstore.promotion.dto.request.UpdateCouponRequest;
import org.akira.laptopstore.promotion.dto.response.CouponResponse;
import org.akira.laptopstore.promotion.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<CouponResponse> create(
            @Valid @RequestBody CreateCouponRequest request) {

        CouponResponse response = couponService.createCoupon(request);

        return ResponseEntity
                .status(201)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAll() {

        return ResponseEntity.ok(
                couponService.getAllCoupons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                couponService.getCouponById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<CouponResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCouponRequest request) {

        return ResponseEntity.ok(
                couponService.updateCoupon(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        couponService.deleteCoupon(id);

        return ResponseEntity.noContent().build();
    }
}