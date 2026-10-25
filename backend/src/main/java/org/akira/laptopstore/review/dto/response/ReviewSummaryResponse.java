package org.akira.laptopstore.review.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Thống kê review đang hiển thị của một sản phẩm.
 * ratingCounts luôn đủ 5 key (1..5), mức sao chưa có review thì = 0.
 */
@Getter
@Builder
public class ReviewSummaryResponse {

    private Long productId;

    private BigDecimal averageRating;

    private long totalReviews;

    private Map<Integer, Long> ratingCounts;
}
