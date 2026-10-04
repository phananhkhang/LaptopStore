package org.akira.laptopstore.common.exception;

import org.akira.laptopstore.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidPaymentStatusException
        extends BusinessException {

    public InvalidPaymentStatusException(String message) {
        super(
                HttpStatus.CONFLICT,
                "INVALID_PAYMENT_STATUS",
                message);
    }
}