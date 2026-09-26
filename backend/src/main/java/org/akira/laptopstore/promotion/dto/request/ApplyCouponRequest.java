package org.akira.laptopstore.promotion.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyCouponRequest {

    private String code;

    private Long orderId;
}