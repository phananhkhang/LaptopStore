package org.akira.laptopstore.order.service;

import java.math.BigDecimal;

import org.akira.laptopstore.order.dto.response.OrderItemReviewInfo;

public interface OrderQueryService {

    BigDecimal getOrderTotalAmount(Long orderId);

    OrderItemReviewInfo getOrderItemForReview(Long orderItemId);

    void validateOrderOwner(Long orderId, Long userId);
}