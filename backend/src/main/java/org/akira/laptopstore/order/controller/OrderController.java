package org.akira.laptopstore.order.controller;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.order.dto.request.CreateOrderRequest;
import org.akira.laptopstore.order.dto.request.UpdateOrderStatusRequest;
import org.akira.laptopstore.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // TODO: Implement order endpoints
    // POST /api/orders - Create order from cart
    // GET /api/orders/{id} - Get order by ID
    // GET /api/orders/my-orders - Get current user's orders
    // GET /api/orders (admin) - Get all orders
    // PUT /api/orders/{id}/status (admin) - Update order status
    // POST /api/orders/{id}/cancel - Cancel order
}
