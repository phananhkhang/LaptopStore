package org.akira.laptopstore.order.service;

import org.akira.laptopstore.order.dto.request.CheckoutPreviewRequest;
import org.akira.laptopstore.order.dto.request.CreateOrderRequest;
import org.akira.laptopstore.order.dto.response.CheckoutPreviewResponse;
import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.akira.laptopstore.order.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    CheckoutPreviewResponse previewCheckout(Long userId, CheckoutPreviewRequest request);

    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    OrderResponse getOwnOrder(Long userId, Long orderId);

    Page<OrderSummaryResponse> getOwnOrders(Long userId, Pageable pageable);

    Page<OrderSummaryResponse> getAllOrders(Pageable pageable);

}
