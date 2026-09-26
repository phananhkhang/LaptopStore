package org.akira.laptopstore.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.order.dto.request.CreateOrderRequest;
import org.akira.laptopstore.order.dto.request.UpdateOrderStatusRequest;
import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.akira.laptopstore.order.mapper.OrderMapper;
import org.akira.laptopstore.order.repository.OrderItemRepository;
import org.akira.laptopstore.order.repository.OrderRepository;
import org.akira.laptopstore.order.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;

    // TODO: Implement service methods

    @Override
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        return null;
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        return null;
    }

    @Override
    public Page<OrderResponse> getOrdersByUser(Long userId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return null;
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, Long changedBy) {
        return null;
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        return null;
    }
}
