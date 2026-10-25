package org.akira.laptopstore.review.service.impl;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.order.dto.response.OrderItemReviewInfo;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.akira.laptopstore.order.service.OrderQueryService;
import org.akira.laptopstore.review.dto.request.CreateReviewRequest;
import org.akira.laptopstore.review.dto.request.UpdateReviewRequest;
import org.akira.laptopstore.review.dto.response.ReviewResponse;
import org.akira.laptopstore.review.dto.response.ReviewSummaryResponse;
import org.akira.laptopstore.review.entity.Review;
import org.akira.laptopstore.review.exception.ReviewAlreadyExistsException;
import org.akira.laptopstore.review.exception.ReviewForbiddenException;
import org.akira.laptopstore.review.exception.ReviewNotAllowedException;
import org.akira.laptopstore.review.repository.ReviewRepository;
import org.akira.laptopstore.review.service.ReviewService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            throw new ReviewAlreadyExistsException("Order item has already been reviewed");
        }

        Review review = Review.builder()
                .orderItemId(request.getOrderItemId())
                .rating(request.getRating())
                .comment(normalizeComment(request.getComment()))
                .visible(true)
                .build();

        Review saved;
        try {
            // flush ngay để UNIQUE(order_item_id) chặn 2 request đồng thời ngay tại đây
            saved = repository.saveAndFlush(review);
        } catch (DataIntegrityViolationException e) {
            throw new ReviewAlreadyExistsException("Order item has already been reviewed");
        }

        return toResponse(saved, orderItem);
    }

    @Override
    public ReviewResponse update(Long userId, Long reviewId, UpdateReviewRequest request) {

        Review review = findReview(reviewId);

        OrderItemReviewInfo orderItem = orderQueryService.getOrderItemForReview(review.getOrderItemId());

        validateOwner(userId, orderItem);

        // Không cho sửa review đã bị staff ẩn, tránh sửa nội dung rồi chờ được hiện lại
        if (!Boolean.TRUE.equals(review.getVisible())) {
            throw new ReviewNotAllowedException("Hidden review cannot be edited");
        }

        if (request.getRating() != null) {
            validateRating(request.getRating());
            review.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            review.setComment(normalizeComment(request.getComment()));
        }

        Review saved = repository.save(review);

        return toResponse(saved, orderItem);
    }

    @Override
    public ReviewResponse setVisibility(Long reviewId, boolean isVisible) {

        Review review = findReview(reviewId);

        review.setVisible(isVisible);

        Review saved = repository.save(review);

        return toResponse(saved, orderQueryService.getOrderItemForReview(saved.getOrderItemId()));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse get(Long reviewId) {

        Review review = repository.findByIdAndVisibleTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        return toResponse(review, orderQueryService.getOrderItemForReview(review.getOrderItemId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {

        List<Long> orderItemIds = orderQueryService.getOrderItemIdsByProduct(productId);

        if (orderItemIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return toResponsePage(repository.findByOrderItemIdInAndVisibleTrue(orderItemIds, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryResponse getProductReviewSummary(Long productId) {

        List<Long> orderItemIds = orderQueryService.getOrderItemIdsByProduct(productId);

        Map<Integer, Long> ratingCounts = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            ratingCounts.put(star, 0L);
        }

        if (!orderItemIds.isEmpty()) {
            for (Object[] row : repository.countVisibleByRating(orderItemIds)) {
                ratingCounts.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
            }
        }

        long total = 0;
        long sum = 0;
        for (Map.Entry<Integer, Long> entry : ratingCounts.entrySet()) {
            total += entry.getValue();
            sum += entry.getKey() * entry.getValue();
        }

        BigDecimal average = total == 0
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);

        return ReviewSummaryResponse.builder()
                .productId(productId)
                .averageRating(average)
                .totalReviews(total)
                .ratingCounts(ratingCounts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {

        List<Long> orderItemIds = orderQueryService.getOrderItemIdsByUser(userId);

        if (orderItemIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return toResponsePage(repository.findByOrderItemIdIn(orderItemIds, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsForStaff(Boolean visible, Integer rating, Pageable pageable) {

        if (rating != null) {
            validateRating(rating);
        }

        Page<Review> reviews;
        if (visible != null && rating != null) {
            reviews = repository.findByVisibleAndRating(visible, rating, pageable);
        } else if (visible != null) {
            reviews = repository.findByVisible(visible, pageable);
        } else if (rating != null) {
            reviews = repository.findByRating(rating, pageable);
        } else {
            reviews = repository.findAll(pageable);
        }

        return toResponsePage(reviews);
    }

    private Review findReview(Long reviewId) {

        return repository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }

    // Lấy thông tin order item cho cả trang trong 1 lần gọi (tránh N+1)
    private Page<ReviewResponse> toResponsePage(Page<Review> reviews) {

        List<Long> ids = reviews.stream().map(Review::getOrderItemId).toList();

        Map<Long, OrderItemReviewInfo> orderItems = orderQueryService.getOrderItemsForReview(ids)
                .stream()
                .collect(Collectors.toMap(OrderItemReviewInfo::getOrderItemId, Function.identity()));

        return reviews.map(review -> toResponse(review, orderItems.get(review.getOrderItemId())));
    }

    private void validateRating(Integer rating) {

        if (rating == null) {
            throw new ValidationException("Rating is required");
        }

        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }
    }

    private void validateOwner(Long userId, OrderItemReviewInfo orderItem) {

        if (!orderItem.getUserId().equals(userId)) {
            throw new ReviewForbiddenException("You cannot review this order item");
        }
    }

    private void validateDelivered(OrderItemReviewInfo orderItem) {

        if (orderItem.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new ReviewNotAllowedException("Order must be delivered before review");
        }
    }

    // Cắt khoảng trắng; comment rỗng thì lưu null
    private String normalizeComment(String comment) {

        if (comment == null) {
            return null;
        }

        String trimmed = comment.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ReviewResponse toResponse(Review review, OrderItemReviewInfo orderItem) {

        ReviewResponse.ReviewResponseBuilder builder = ReviewResponse.builder()
                .id(review.getId())
                .orderItemId(review.getOrderItemId())
                .rating(review.getRating())
                .comment(review.getComment())
                .visible(review.getVisible())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt());

        if (orderItem != null) {
            builder.productId(orderItem.getProductId())
                    .variantId(orderItem.getVariantId())
                    .productName(orderItem.getProductName())
                    .variantName(orderItem.getVariantName());
        }

        return builder.build();
    }
}
