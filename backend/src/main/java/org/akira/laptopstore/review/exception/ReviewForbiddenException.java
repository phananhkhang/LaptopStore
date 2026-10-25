package org.akira.laptopstore.review.exception;

import org.akira.laptopstore.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ReviewForbiddenException extends BusinessException {
    public ReviewForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "REVIEW_FORBIDDEN", message);
    }
}
