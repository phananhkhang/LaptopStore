package org.akira.laptopstore.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.order.dto.response.OrderItemReviewInfo;
import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.order.entity.OrderItem;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.akira.laptopstore.order.repository.OrderItemRepository;
import org.akira.laptopstore.order.repository.OrderRepository;
import org.akira.laptopstore.order.service.OrderQueryService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public BigDecimal getOrderTotalAmount(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        return order.getTotalAmount();
    }

    @Override
    public OrderItemReviewInfo getOrderItemForReview(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found: " + orderItemId));

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
        Order order = findOrderOrThrow(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not own this order");
        }
    }

    @Override
    public void validateOrderPayable(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ValidationException("Order cannot be paid in current status");
        }
    }

    @Override
    public void validateOrderDelivered(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ValidationException("COD can only be collected after delivery");
        }
    }

    private Order findOrderOrThrow(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new ValidationException("orderId must be greater than 0");
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
}