package org.akira.laptopstore.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.review.dto.request.UpdateReviewVisibilityRequest;
import org.akira.laptopstore.review.dto.response.ReviewResponse;
import org.akira.laptopstore.review.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý review dành cho STAFF và ADMIN.
 *
 * Cho phép xem review, lọc review
 * và thay đổi trạng thái hiển thị.
 */
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
@RestController
@RequestMapping("/api/v1/staff/reviews")
@RequiredArgsConstructor
public class StaffReviewController {

    private final ReviewService reviewService;

    /**
     * Lấy danh sách review dành cho STAFF/ADMIN.
     *
     * Hỗ trợ lọc theo:
     * - visible: đang hiển thị hay bị ẩn;
     * - rating: số sao.
     *
     * Ví dụ:
     * GET /api/v1/staff/reviews?visible=false&rating=1
     */
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getAll(
            @RequestParam(required = false) Boolean visible,
            @RequestParam(required = false) Integer rating,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(
                reviewService.getReviewsForStaff(
                        visible,
                        rating,
                        pageable));
    }

    /**
     * STAFF/ADMIN ẩn hoặc hiện một review.
     *
     * visible = false: ẩn review khỏi phía public.
     * visible = true: cho phép hiển thị lại review.
     */
    @PatchMapping("/{reviewId}/visibility")
    public ResponseEntity<ReviewResponse> updateVisibility(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewVisibilityRequest request) {

        return ResponseEntity.ok(
                reviewService.setVisibility(
                        reviewId,
                        request.getVisible()));
    }
}
