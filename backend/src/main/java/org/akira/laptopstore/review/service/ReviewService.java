package org.akira.laptopstore.review.service;

import java.util.List;

import org.akira.laptopstore.review.dto.request.CreateReviewRequest;
import org.akira.laptopstore.review.dto.request.UpdateReviewRequest;
import org.akira.laptopstore.review.dto.response.ReviewResponse;

public interface ReviewService {
    ReviewResponse create(Long userId, CreateReviewRequest request);

    ReviewResponse update(Long userId, Long reviewId, UpdateReviewRequest request);

    ReviewResponse setVisibility(Long reviewId, boolean isVisible);

    ReviewResponse get(Long reviewId);

    List<ReviewResponse> getVisibleReviews();
}
