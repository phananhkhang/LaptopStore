package org.akira.laptopstore.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.review.dto.request.UpdateReviewVisibilityRequest;
import org.akira.laptopstore.review.dto.response.ReviewResponse;
import org.akira.laptopstore.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
@RestController
@RequestMapping("/api/v1/staff/reviews")
@RequiredArgsConstructor
public class StaffReviewController {

    private final ReviewService reviewService;

    @PutMapping("/{reviewId}/visibility")
    public ResponseEntity<ReviewResponse> updateVisibility(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewVisibilityRequest request) {

        return ResponseEntity.ok(
                reviewService.setVisibility(reviewId, request.getVisible()));
    }
}