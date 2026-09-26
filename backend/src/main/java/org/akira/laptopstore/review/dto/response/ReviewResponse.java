package org.akira.laptopstore.review.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ReviewResponse {

    private Long id;

    private Long orderItemId;

    private Integer rating;

    private String comment;

    private Boolean visible;

    private Instant createdAt;

    private Instant updatedAt;
}