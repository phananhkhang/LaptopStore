package org.akira.laptopstore.promotion.exception;

import org.akira.laptopstore.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CouponCodeConflictException extends BusinessException {
    public CouponCodeConflictException(String message) {
        super(HttpStatus.CONFLICT, "COUPON_CODE_EXISTS", message);
    }
}
