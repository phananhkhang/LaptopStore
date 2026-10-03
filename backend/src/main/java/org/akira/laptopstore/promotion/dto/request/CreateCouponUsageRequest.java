package org.akira.laptopstore.promotion.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateCouponUsageRequest {

    private Long couponId;

    private Long userId;

    private Long orderId;

    private BigDecimal discountAmount;
}