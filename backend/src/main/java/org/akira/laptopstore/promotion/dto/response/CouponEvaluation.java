package org.akira.laptopstore.promotion.dto.response;

import java.math.BigDecimal;

public record CouponEvaluation(Long couponId,String couponCode,BigDecimal discountAmount
) {
}