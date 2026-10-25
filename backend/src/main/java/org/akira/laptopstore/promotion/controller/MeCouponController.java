package org.akira.laptopstore.promotion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.promotion.dto.request.PreviewCouponRequest;
import org.akira.laptopstore.promotion.dto.response.CouponEvaluation;
import org.akira.laptopstore.promotion.dto.response.CouponUsageResponse;
import org.akira.laptopstore.promotion.service.CouponService;
import org.akira.laptopstore.promotion.service.CouponUsageService;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me/coupons")
@RequiredArgsConstructor
public class MeCouponController {

    private final CouponService couponService;
    private final CouponUsageService couponUsageService;

    // Chỉ xem trước, không giữ lượt; lượt được ghi khi đặt hàng (reserveForOrder).
    @PostMapping("/preview")
    public ResponseEntity<CouponEvaluation> preview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PreviewCouponRequest request) {

        return ResponseEntity.ok(
                couponService.evaluateForCheckout(
                        request.getCode(),
                        userDetails.getId(),
                        request.getSubtotal()));
    }

    @GetMapping("/usages")
    public ResponseEntity<List<CouponUsageResponse>> getMyUsages(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                couponUsageService.getByUserId(userDetails.getId()));
    }
}
