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

@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
@RestController
@RequestMapping("/api/v1/staff/reviews")
@RequiredArgsConstructor
public class StaffReviewController {

    private final ReviewService reviewService;

    // Lọc tùy chọn: ?visible=false&rating=1
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getAll(
            @RequestParam(required = false) Boolean visible,
            @RequestParam(required = false) Integer rating,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(
                reviewService.getReviewsForStaff(visible, rating, pageable));
    }

    @PatchMapping("/{reviewId}/visibility")
    public ResponseEntity<ReviewResponse> updateVisibility(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewVisibilityRequest request) {

        return ResponseEntity.ok(
                reviewService.setVisibility(reviewId, request.getVisible()));
    }
}
