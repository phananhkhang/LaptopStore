package org.akira.laptopstore.review.service;

import org.akira.laptopstore.review.dto.request.CreateReviewRequest;
import org.akira.laptopstore.review.dto.request.UpdateReviewRequest;
import org.akira.laptopstore.review.dto.response.ReviewResponse;
import org.akira.laptopstore.review.dto.response.ReviewSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse create(Long userId, CreateReviewRequest request);

    ReviewResponse update(Long userId, Long reviewId, UpdateReviewRequest request);

    ReviewResponse setVisibility(Long reviewId, boolean isVisible);

    // Public: chỉ trả review đang hiển thị
    ReviewResponse get(Long reviewId);

    Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable);

    ReviewSummaryResponse getProductReviewSummary(Long productId);

    // Gồm cả review đã bị ẩn để user biết trạng thái
    Page<ReviewResponse> getMyReviews(Long userId, Pageable pageable);

    // visible/rating = null nghĩa là không lọc
    Page<ReviewResponse> getReviewsForStaff(Boolean visible, Integer rating, Pageable pageable);
}
