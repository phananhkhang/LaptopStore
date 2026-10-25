package org.akira.laptopstore.review.exception;

import org.akira.laptopstore.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ReviewAlreadyExistsException extends BusinessException {
    public ReviewAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, "REVIEW_ALREADY_EXISTS", message);
    }
}
