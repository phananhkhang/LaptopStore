package org.akira.laptopstore.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.akira.laptopstore.review.dto.request.CreateReviewRequest;
import org.akira.laptopstore.review.dto.request.UpdateReviewRequest;
import org.akira.laptopstore.review.dto.response.ReviewResponse;
import org.akira.laptopstore.review.dto.response.ReviewSummaryResponse;
import org.akira.laptopstore.review.service.ReviewService;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý review phía khách hàng
 * và các API review công khai.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

        private final ReviewService reviewService;

        /**
         * USER tạo review cho sản phẩm đã mua.
         *
         * Service kiểm tra:
         * - order item thuộc user;
         * - đơn đã DELIVERED;
         * - order item chưa từng được review.
         */
        @PreAuthorize("hasRole('USER')")
        @PostMapping("/me/reviews")
        public ResponseEntity<ReviewResponse> create(
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        @Valid @RequestBody CreateReviewRequest request) {

                ReviewResponse response = reviewService.create(
                                userDetails.getId(),
                                request);

                return ResponseEntity.status(201).body(response);
        }

        /**
         * USER cập nhật review của chính mình.
         *
         * User không thể sửa review của người khác.
         */
        @PreAuthorize("hasRole('USER')")
        @PutMapping("/me/reviews/{reviewId}")
        public ResponseEntity<ReviewResponse> update(
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        @PathVariable Long reviewId,
                        @Valid @RequestBody UpdateReviewRequest request) {

                ReviewResponse response = reviewService.update(
                                userDetails.getId(),
                                reviewId,
                                request);

                return ResponseEntity.ok(response);
        }

        /**
         * Lấy lịch sử review của user đang đăng nhập.
         *
         * Có phân trang và mặc định sắp xếp
         * review mới nhất trước.
         */
        @GetMapping("/me/reviews")
        public ResponseEntity<Page<ReviewResponse>> getMyReviews(
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                return ResponseEntity.ok(
                                reviewService.getMyReviews(
                                                userDetails.getId(),
                                                pageable));
        }

        /**
         * Lấy chi tiết một review đang được public.
         */
        @GetMapping("/reviews/{reviewId}")
        public ResponseEntity<ReviewResponse> get(
                        @PathVariable Long reviewId) {

                return ResponseEntity.ok(
                                reviewService.get(reviewId));
        }

        /**
         * Lấy danh sách review đang hiển thị
         * của một sản phẩm.
         *
         * Có phân trang và mặc định review mới nhất trước.
         */
        @GetMapping("/products/{productId}/reviews")
        public ResponseEntity<Page<ReviewResponse>> getProductReviews(
                        @PathVariable Long productId,
                        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

                return ResponseEntity.ok(
                                reviewService.getProductReviews(
                                                productId,
                                                pageable));
        }

        /**
         * Lấy thống kê review của sản phẩm.
         *
         * Bao gồm điểm trung bình, tổng số review
         * và phân bố số lượng review theo từng mức sao.
         */
        @GetMapping("/products/{productId}/reviews/summary")
        public ResponseEntity<ReviewSummaryResponse> getProductReviewSummary(
                        @PathVariable Long productId) {

                return ResponseEntity.ok(
                                reviewService.getProductReviewSummary(productId));
        }
}