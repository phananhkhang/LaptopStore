package org.akira.laptopstore.order.service.impl;

import lombok.RequiredArgsConstructor;

import org.akira.laptopstore.order.dto.response.OrderItemReviewInfo;
import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.order.entity.OrderItem;
import org.akira.laptopstore.order.repository.OrderItemRepository;
import org.akira.laptopstore.order.repository.OrderRepository;
import org.akira.laptopstore.order.service.OrderQueryService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;

        @Override
        public BigDecimal getOrderTotalAmount(Long orderId) {

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(
                                                () -> new RuntimeException(
                                                                "Order not found"));

                return order.getTotalAmount();
        }

        @Override
        public OrderItemReviewInfo getOrderItemForReview(Long orderItemId) {

                OrderItem orderItem = orderItemRepository.findById(orderItemId)
                                .orElseThrow(
                                                () -> new RuntimeException(
                                                                "Order item not found: " + orderItemId));

                Order order = orderItem.getOrder();

                return OrderItemReviewInfo.builder()
                                .orderItemId(orderItem.getId())
                                .orderId(order.getId())
                                .userId(order.getUserId())
                                .productId(orderItem.getProductId())
                                .variantId(orderItem.getVariantId())
                                .orderStatus(order.getStatus())
                                .build();
        }

        @Override
        public void validateOrderOwner(Long orderId, Long userId) {

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(
                                                () -> new RuntimeException("Order not found"));

                if (!order.getUserId().equals(userId)) {
                        throw new AccessDeniedException(
                                        "You do not own this order");
                }
        }
}