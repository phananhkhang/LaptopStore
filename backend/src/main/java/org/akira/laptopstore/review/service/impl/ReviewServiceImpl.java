package org.akira.laptopstore.review.service.impl;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.akira.laptopstore.order.dto.response.OrderItemReviewInfo;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.akira.laptopstore.order.service.OrderQueryService;
import org.akira.laptopstore.review.dto.request.CreateReviewRequest;
import org.akira.laptopstore.review.dto.request.UpdateReviewRequest;
import org.akira.laptopstore.review.dto.response.ReviewResponse;
import org.akira.laptopstore.review.entity.Review;
import org.akira.laptopstore.review.repository.ReviewRepository;
import org.akira.laptopstore.review.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl
        implements ReviewService {

    private final ReviewRepository repository;

    private final OrderQueryService orderQueryService;

    @Override
    public ReviewResponse create(Long userId, CreateReviewRequest request) {

        validateRating(request.getRating());

        OrderItemReviewInfo orderItem = orderQueryService.getOrderItemForReview(request.getOrderItemId());

        validateOwner(userId, orderItem);

        validateDelivered(orderItem);

        if (repository.existsByOrderItemId(request.getOrderItemId())) {

            throw new RuntimeException("Order item has already been reviewed");
        }

        Review review = Review.builder()
                .orderItemId(request.getOrderItemId())
                .rating(request.getRating())
                .comment(request.getComment())
                .visible(true)
                .build();

        Review saved = repository.save(review);

        return toResponse(saved);
    }

    @Override
    public ReviewResponse update(Long userId, Long reviewId, UpdateReviewRequest request) {

        Review review = repository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));

        OrderItemReviewInfo orderItem = orderQueryService.getOrderItemForReview(review.getOrderItemId());

        validateOwner(userId, orderItem);

        if (request.getRating() != null) {
            validateRating(request.getRating());
            review.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        Review saved = repository.save(review);

        return toResponse(saved);
    }

    @Override
    public ReviewResponse setVisibility(Long reviewId, boolean isVisible) {

        Review review = repository.findById(reviewId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Review not found"));

        review.setVisible(isVisible);

        Review saved = repository.save(review);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse get(Long reviewId) {

        Review review = repository.findById(reviewId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Review not found"));

        return toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getVisibleReviews() {

        return repository
                .findByVisibleTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateRating(Integer rating) {

        if (rating == null) {
            throw new RuntimeException(
                    "Rating is required");
        }

        if (rating < 1 || rating > 5) {
            throw new RuntimeException(
                    "Rating must be between 1 and 5");
        }
    }

    private void validateOwner(Long userId, OrderItemReviewInfo orderItem) {

        if (!orderItem.getUserId().equals(userId)) {

            throw new RuntimeException(
                    "You cannot review this order item");
        }
    }

    private void validateDelivered(OrderItemReviewInfo orderItem) {

        if (orderItem.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException(
                    "Order must be delivered before review");
        }
    }

    private ReviewResponse toResponse(Review review) {

        return ReviewResponse.builder()
                .id(review.getId())
                .orderItemId(review.getOrderItemId())
                .rating(review.getRating())
                .comment(review.getComment())
                .visible(review.getVisible())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}