package org.akira.laptopstore.common.exception;

import org.akira.laptopstore.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class PaymentNotFoundException
        extends BusinessException {

    public PaymentNotFoundException(String message) {
        super(
                HttpStatus.NOT_FOUND,
                "PAYMENT_NOT_FOUND",
                message);
    }
}