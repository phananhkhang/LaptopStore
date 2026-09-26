package org.akira.laptopstore.promotion.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class CreateCouponRequest {

    private String code;

    private String discountType;

    private BigDecimal discountValue;

    private BigDecimal maxDiscount;

    private BigDecimal minOrderAmount;

    private Integer usageLimit;

    private Integer usagePerUser;

    private Instant startsAt;

    private Instant endsAt;
}