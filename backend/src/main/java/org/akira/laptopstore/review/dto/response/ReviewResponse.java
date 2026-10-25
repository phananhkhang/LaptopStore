package org.akira.laptopstore.review.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ReviewResponse {

    private Long id;

    private Long orderItemId;

    // Lấy từ order item (snapshot lúc mua), không lưu trong bảng reviews
    private Long productId;

    private Long variantId;

    private String productName;

    private String variantName;

    private Integer rating;

    private String comment;

    private Boolean visible;

    private Instant createdAt;

    private Instant updatedAt;
}
