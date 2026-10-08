package org.akira.laptopstore.common.exception;

import org.akira.laptopstore.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class PaymentAlreadyExistsException
        extends BusinessException {

    public PaymentAlreadyExistsException(String message) {
        super(
                HttpStatus.CONFLICT,
                "PAYMENT_ALREADY_EXISTS",
                message);
    }
}