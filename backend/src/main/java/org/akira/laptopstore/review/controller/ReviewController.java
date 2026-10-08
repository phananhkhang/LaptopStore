package org.akira.laptopstore.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.akira.laptopstore.review.dto.request.CreateReviewRequest;
import org.akira.laptopstore.review.dto.request.UpdateReviewRequest;
import org.akira.laptopstore.review.dto.response.ReviewResponse;
import org.akira.laptopstore.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> create(
            @Valid @RequestBody CreateReviewRequest request) {

        // hard-code user
        Long userId = 1L;

        ReviewResponse response = reviewService.create(userId, request);

        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> update(@PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {

        Long userId = 1L;

        ReviewResponse response = reviewService.update(
                userId,
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