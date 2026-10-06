package org.akira.laptopstore.promotion.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.akira.laptopstore.promotion.enums.CouponUsageStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class CouponUsageResponse {

    private Long id;

    private Long couponId;

    private String couponCode;

    private Long userId;

    private Long orderId;

    private BigDecimal discountAmount;

    private CouponUsageStatus status;

    private Instant createdAt;
}