package org.akira.laptopstore.order.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.akira.laptopstore.order.dto.response.OrderItemReviewInfo;

public interface OrderQueryService {

    BigDecimal getOrderTotalAmount(Long orderId);

    OrderItemReviewInfo getOrderItemForReview(Long orderItemId);

    // Contract cho review: lấy nhiều order item một lần để tránh N+1
    List<OrderItemReviewInfo> getOrderItemsForReview(Collection<Long> orderItemIds);

    // Contract cho review: id các order item của một sản phẩm (review theo sản phẩm, điểm trung bình)
    List<Long> getOrderItemIdsByProduct(Long productId);

    // Contract cho review: id các order item của một user (review của tôi)
    List<Long> getOrderItemIdsByUser(Long userId);

    void validateOrderOwner(Long orderId, Long userId);

    void validateOrderPayable(Long orderId);

    void validateOrderDelivered(Long orderId);
}
