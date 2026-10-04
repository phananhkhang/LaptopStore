package org.akira.laptopstore.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.order.repository.OrderRepository;
import org.akira.laptopstore.order.service.OrderQueryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;

    @Override
    public BigDecimal getOrderTotalAmount(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Order not found"));

        return order.getTotalAmount();
    }
}