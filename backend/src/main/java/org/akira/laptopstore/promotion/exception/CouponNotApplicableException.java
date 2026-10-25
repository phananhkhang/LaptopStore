package org.akira.laptopstore.promotion.exception;

import org.akira.laptopstore.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CouponNotApplicableException extends BusinessException {
    public CouponNotApplicableException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "COUPON_NOT_APPLICABLE", message);
    }
}
