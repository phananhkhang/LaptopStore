package org.akira.laptopstore.review.fakeOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderItemReviewInfo {

    private Long orderItemId;

    private Long userId;

    private Long productId;

    private String orderStatus;
}