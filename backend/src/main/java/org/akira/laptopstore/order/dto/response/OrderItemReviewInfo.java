package org.akira.laptopstore.order.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.akira.laptopstore.order.entity.enums.OrderStatus;

@Getter
@Builder
public class OrderItemReviewInfo {

    private Long orderItemId;

    private Long orderId;

    private Long userId;

    private Long productId;

    private Long variantId;

    private OrderStatus orderStatus;
}