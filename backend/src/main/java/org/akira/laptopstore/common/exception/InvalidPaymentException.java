package org.akira.laptopstore.common.exception;

import org.akira.laptopstore.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidPaymentException
        extends BusinessException {

    public InvalidPaymentException(String message) {
        super(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAYMENT",
                message);
    }
}