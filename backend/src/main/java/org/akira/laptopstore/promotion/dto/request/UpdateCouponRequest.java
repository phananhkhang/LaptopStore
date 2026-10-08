package org.akira.laptopstore.promotion.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.akira.laptopstore.promotion.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class UpdateCouponRequest {

    private String code;

    private DiscountType discountType;

    private BigDecimal discountValue;

    private BigDecimal maxDiscount;

    private BigDecimal minOrderAmount;

    private Integer usageLimit;

    private Integer usagePerUser;

    private Instant startsAt;

    private Instant endsAt;

    private boolean active;
}