package org.akira.laptopstore.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.akira.laptopstore.order.dto.response.OrderSummaryResponse;
import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.UnauthenticatedException;
import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.order.mapper.OrderMapper;
import org.akira.laptopstore.order.repository.OrderItemRepository;
import org.akira.laptopstore.order.repository.OrderRepository;
import org.akira.laptopstore.order.repository.OrderStatusHistoryRepository;
import org.akira.laptopstore.order.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOwnOrder(Long userId, Long orderId) {
        requireUserId(userId);
        requireOrderId(orderId);
        Order order = orderRepository.findById(orderId)
                .filter(candidate -> userId.equals(candidate.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return orderMapper.toResponse(order, orderItemRepository.findByOrderId(orderId),
                orderStatusHistoryRepository.findByOrderIdOrderByChangedAtDesc(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOwnOrders(Long userId, Pageable pageable) {
        requireUserId(userId);
        return orderRepository.findByUserId(userId, pageable).map(this::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toSummaryResponse);
    }

    private OrderSummaryResponse toSummaryResponse(Order order) {
        int totalItems = orderItemRepository.findByOrderId(order.getId()).stream()
                .mapToInt(item -> item.getQuantity())
                .sum();
        return orderMapper.toSummaryResponse(order, totalItems);
    }

    private void requireUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new UnauthenticatedException("Invalid authenticated user");
        }
    }

    private void requireOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new ValidationException("orderId must be greater than 0");
        }
    }
}
