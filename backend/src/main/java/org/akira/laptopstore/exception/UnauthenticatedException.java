package org.akira.laptopstore.exception;

import org.springframework.http.HttpStatus;

public class UnauthenticatedException extends BusinessException {
    public UnauthenticatedException(String message) {
        super(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", message);
    }
}
