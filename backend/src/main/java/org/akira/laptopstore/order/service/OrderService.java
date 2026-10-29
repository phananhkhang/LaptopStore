package org.akira.laptopstore.order.service;

import org.akira.laptopstore.order.dto.request.CheckoutPreviewRequest;
import org.akira.laptopstore.order.dto.request.CreateOrderRequest;
import org.akira.laptopstore.order.dto.request.UpdateOrderStatusRequest;
import org.akira.laptopstore.order.dto.response.CheckoutPreviewResponse;
import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.akira.laptopstore.order.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Duration;

public interface OrderService {

    CheckoutPreviewResponse previewCheckout(Long userId, CheckoutPreviewRequest request);

    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    OrderResponse getStaffOrder(Long orderId);

    OrderResponse getOwnOrder(Long userId, Long orderId);

    Page<OrderSummaryResponse> getOwnOrders(Long userId, Pageable pageable);

    Page<OrderSummaryResponse> searchOrders(Long userId, org.akira.laptopstore.order.entity.enums.OrderStatus status, String search, Pageable pageable);

    Page<OrderSummaryResponse> getAllOrders(Pageable pageable);

    OrderResponse updateOrderStatus(Long actorId, Long orderId, UpdateOrderStatusRequest request);

    OrderResponse cancelOrder(Long userId, Long orderId, String reason);

    int expirePendingOrders(Duration timeout);

    int expirePendingOrders();
}
