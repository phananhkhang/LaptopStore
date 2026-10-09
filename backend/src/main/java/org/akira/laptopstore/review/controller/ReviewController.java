package org.akira.laptopstore.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.akira.laptopstore.review.dto.request.CreateReviewRequest;
import org.akira.laptopstore.review.dto.request.UpdateReviewRequest;
import org.akira.laptopstore.review.dto.response.ReviewResponse;
import org.akira.laptopstore.review.service.ReviewService;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ReviewResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse response = reviewService.create(
                userDetails.getId(),
                request);

        return ResponseEntity.status(201).body(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{reviewId}")
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

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> get(@PathVariable Long reviewId) {

        return ResponseEntity.ok(
                reviewService.get(reviewId));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getAll() {

        return ResponseEntity.ok(
                reviewService.getVisibleReviews());
    }
}