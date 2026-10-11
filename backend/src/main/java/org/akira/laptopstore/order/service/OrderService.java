package org.akira.laptopstore.order.service;

import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.akira.laptopstore.order.dto.response.OrderSummaryResponse;
import org.akira.laptopstore.order.dto.request.CreateOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    OrderResponse getOwnOrder(Long userId, Long orderId);

    Page<OrderSummaryResponse> getOwnOrders(Long userId, Pageable pageable);

    Page<OrderSummaryResponse> getAllOrders(Pageable pageable);

}
