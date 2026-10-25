package org.akira.laptopstore.review.exception;

import org.akira.laptopstore.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ReviewNotAllowedException extends BusinessException {
    public ReviewNotAllowedException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "REVIEW_NOT_ALLOWED", message);
    }
}
