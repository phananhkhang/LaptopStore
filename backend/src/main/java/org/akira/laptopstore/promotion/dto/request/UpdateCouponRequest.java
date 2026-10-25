package org.akira.laptopstore.promotion.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.akira.laptopstore.promotion.enums.DiscountType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class UpdateCouponRequest {
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,30}$")
    private String code;

    @NotNull
    private DiscountType discountType;

    @NotNull
    @Positive
    private BigDecimal discountValue;

    @PositiveOrZero
    private BigDecimal maxDiscount;

    @PositiveOrZero
    private BigDecimal minOrderAmount;

    @Positive
    private Integer usageLimit;

    @Positive
    private Integer usagePerUser;

    private Instant startsAt;

    private Instant endsAt;

    // null = giữ nguyên trạng thái hiện tại
    private Boolean active;
}
