package org.akira.laptopstore.review.service;

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
import org.akira.laptopstore.review.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {
    @Mock private ReviewRepository reviews;
    @Mock private OrderQueryService orders;
    private ReviewServiceImpl service;
    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        service = new ReviewServiceImpl(reviews, orders);
    }

    // ---------- Tạo review ----------

    @Test
    void createSavesTrimmedCommentAndReturnsProductInfo() {
        when(orders.getOrderItemForReview(101L)).thenReturn(item(101L, 42L, OrderStatus.DELIVERED));
        when(reviews.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse result = service.create(42L, createRequest(101L, 5, "  Máy chạy mượt  "));

        assertEquals("Máy chạy mượt", result.getComment());
        assertEquals(7L, result.getProductId());
        assertEquals("16GB / 512GB", result.getVariantName());
        assertTrue(result.getVisible());
    }

    @Test
    void createStoresBlankCommentAsNull() {
        when(orders.getOrderItemForReview(101L)).thenReturn(item(101L, 42L, OrderStatus.DELIVERED));
        when(reviews.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertNull(service.create(42L, createRequest(101L, 4, "   ")).getComment());
    }

    @Test
    void createRejectsOrderItemOfAnotherUser() {
        when(orders.getOrderItemForReview(101L)).thenReturn(item(101L, 7777L, OrderStatus.DELIVERED));

        assertThrows(ReviewForbiddenException.class,
                () -> service.create(42L, createRequest(101L, 5, null)));
        verify(reviews, never()).saveAndFlush(any());
    }

    @Test
    void createRejectsOrderNotDelivered() {
        when(orders.getOrderItemForReview(101L)).thenReturn(item(101L, 42L, OrderStatus.SHIPPING));

        assertThrows(ReviewNotAllowedException.class,
                () -> service.create(42L, createRequest(101L, 5, null)));
    }

    @Test
    void createRejectsUnknownOrderItem() {
        when(orders.getOrderItemForReview(999L)).thenThrow(new ResourceNotFoundException("Order item not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> service.create(42L, createRequest(999L, 5, null)));
    }

    @Test
    void createRejectsDuplicateReview() {
        when(orders.getOrderItemForReview(101L)).thenReturn(item(101L, 42L, OrderStatus.DELIVERED));
        when(reviews.existsByOrderItemId(101L)).thenReturn(true);

        assertThrows(ReviewAlreadyExistsException.class,
                () -> service.create(42L, createRequest(101L, 5, null)));
    }

    @Test
    void createMapsConcurrentUniqueViolationTo409() {
        when(orders.getOrderItemForReview(101L)).thenReturn(item(101L, 42L, OrderStatus.DELIVERED));
        when(reviews.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uq_reviews_order_item"));

        assertThrows(ReviewAlreadyExistsException.class,
                () -> service.create(42L, createRequest(101L, 5, null)));
    }

    @Test
    void createRejectsRatingOutOfRange() {
        assertThrows(ValidationException.class, () -> service.create(42L, createRequest(101L, 0, null)));
        assertThrows(ValidationException.class, () -> service.create(42L, createRequest(101L, 6, null)));
        verifyNoInteractions(orders);
    }

    // ---------- Sửa review ----------

    @Test
    void updateRejectsReviewOfAnotherUser() {
        when(reviews.findById(1L)).thenReturn(Optional.of(review(1L, 101L, true)));
        when(orders.getOrderItemForReview(101L)).thenReturn(item(101L, 7777L, OrderStatus.DELIVERED));

        assertThrows(ReviewForbiddenException.class,
                () -> service.update(42L, 1L, updateRequest(3)));
    }

    @Test
    void updateRejectsHiddenReview() {
        when(reviews.findById(1L)).thenReturn(Optional.of(review(1L, 101L, false)));
        when(orders.getOrderItemForReview(101L)).thenReturn(item(101L, 42L, OrderStatus.DELIVERED));

        assertThrows(ReviewNotAllowedException.class,
                () -> service.update(42L, 1L, updateRequest(3)));
        verify(reviews, never()).save(any());
    }

    @Test
    void updateChangesOnlySentFields() {
        Review review = review(1L, 101L, true);
        when(reviews.findById(1L)).thenReturn(Optional.of(review));
        when(orders.getOrderItemForReview(101L)).thenReturn(item(101L, 42L, OrderStatus.DELIVERED));
        when(reviews.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse result = service.update(42L, 1L, updateRequest(3));

        assertEquals(3, result.getRating());
        assertEquals("Tốt", result.getComment());
    }

    @Test
    void updateMissingReviewThrowsNotFound() {
        when(reviews.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(42L, 1L, updateRequest(3)));
    }

    // ---------- Xem review ----------

    @Test
    void getHiddenReviewThrowsNotFound() {
        when(reviews.findByIdAndVisibleTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.get(1L));
    }

    @Test
    void productReviewsWithoutOrderItemsReturnsEmptyPageWithoutQuery() {
        when(orders.getOrderItemIdsByProduct(7L)).thenReturn(List.of());

        assertTrue(service.getProductReviews(7L, pageable).isEmpty());
        verify(reviews, never()).findByOrderItemIdInAndVisibleTrue(any(), any());
    }

    @Test
    void productReviewsLoadOrderItemsInOneBatch() {
        when(orders.getOrderItemIdsByProduct(7L)).thenReturn(List.of(101L, 102L, 103L));
        when(reviews.findByOrderItemIdInAndVisibleTrue(List.of(101L, 102L, 103L), pageable))
                .thenReturn(new PageImpl<>(List.of(review(1L, 101L, true), review(2L, 102L, true))));
        when(orders.getOrderItemsForReview(List.of(101L, 102L)))
                .thenReturn(List.of(item(101L, 42L, OrderStatus.DELIVERED), item(102L, 43L, OrderStatus.DELIVERED)));

        Page<ReviewResponse> result = service.getProductReviews(7L, pageable);

        assertEquals(2, result.getNumberOfElements());
        assertEquals(7L, result.getContent().get(1).getProductId());
        verify(orders, times(1)).getOrderItemsForReview(any());
        verify(orders, never()).getOrderItemForReview(any());
    }

    @Test
    void summaryComputesAverageAndFillsAllStars() {
        when(orders.getOrderItemIdsByProduct(7L)).thenReturn(List.of(101L, 102L, 103L));
        when(reviews.countVisibleByRating(List.of(101L, 102L, 103L))).thenReturn(List.of(
                new Object[]{5, 2L},
                new Object[]{4, 1L}));

        ReviewSummaryResponse summary = service.getProductReviewSummary(7L);

        assertEquals(3, summary.getTotalReviews());
        assertEquals(new BigDecimal("4.7"), summary.getAverageRating());
        assertEquals(5, summary.getRatingCounts().size());
        assertEquals(0L, summary.getRatingCounts().get(1));
        assertEquals(2L, summary.getRatingCounts().get(5));
    }

    @Test
    void summaryForProductWithoutReviewsIsZero() {
        when(orders.getOrderItemIdsByProduct(7L)).thenReturn(List.of());

        ReviewSummaryResponse summary = service.getProductReviewSummary(7L);

        assertEquals(0, summary.getTotalReviews());
        assertEquals(new BigDecimal("0.0"), summary.getAverageRating());
    }

    @Test
    void myReviewsUseOrderItemsOfUser() {
        when(orders.getOrderItemIdsByUser(42L)).thenReturn(List.of(101L));
        when(reviews.findByOrderItemIdIn(List.of(101L), pageable))
                .thenReturn(new PageImpl<>(List.of(review(1L, 101L, false))));
        when(orders.getOrderItemsForReview(List.of(101L)))
                .thenReturn(List.of(item(101L, 42L, OrderStatus.DELIVERED)));

        Page<ReviewResponse> result = service.getMyReviews(42L, pageable);

        // review đã ẩn vẫn hiện trong danh sách của chính chủ
        assertFalse(result.getContent().get(0).getVisible());
    }

    @Test
    void staffListAppliesFilters() {
        when(reviews.findByVisibleAndRating(false, 1, pageable)).thenReturn(Page.empty());

        service.getReviewsForStaff(false, 1, pageable);

        verify(reviews).findByVisibleAndRating(false, 1, pageable);
        verify(reviews, never()).findAll(any(Pageable.class));
    }

    private CreateReviewRequest createRequest(Long orderItemId, Integer rating, String comment) {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setOrderItemId(orderItemId);
        request.setRating(rating);
        request.setComment(comment);
        return request;
    }

    private UpdateReviewRequest updateRequest(Integer rating) {
        UpdateReviewRequest request = new UpdateReviewRequest();
        request.setRating(rating);
        return request;
    }

    private Review review(Long id, Long orderItemId, boolean visible) {
        return Review.builder()
                .id(id)
                .orderItemId(orderItemId)
                .rating(5)
                .comment("Tốt")
                .visible(visible)
                .build();
    }

    private OrderItemReviewInfo item(Long orderItemId, Long userId, OrderStatus status) {
        return OrderItemReviewInfo.builder()
                .orderItemId(orderItemId)
                .orderId(500L)
                .userId(userId)
                .productId(7L)
                .variantId(70L)
                .productName("MacBook Air M3")
                .variantName("16GB / 512GB")
                .orderStatus(status)
                .build();
    }
}
