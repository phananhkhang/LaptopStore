package org.akira.laptopstore.order.service;

import org.akira.laptopstore.order.dto.request.CreateOrderRequest;
import org.akira.laptopstore.order.dto.request.UpdateOrderStatusRequest;
import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    OrderResponse getOrderById(Long orderId);

    Page<OrderResponse> getOrdersByUser(Long userId, Pageable pageable);

    Page<OrderResponse> getAllOrders(Pageable pageable);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, Long changedBy);

    OrderResponse cancelOrder(Long orderId, Long userId);
}
