package org.akira.laptopstore.order.service.impl;

import java.time.Instant;

import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.order.entity.OrderStatusHistory;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.akira.laptopstore.order.repository.OrderRepository;
import org.akira.laptopstore.order.repository.OrderStatusHistoryRepository;
import org.akira.laptopstore.order.service.OrderPaymentService;
import org.akira.laptopstore.order.statemachine.OrderStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderPaymentServiceImpl
        implements OrderPaymentService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    @Transactional
    public boolean confirmOnlinePayment(Long orderId) {

        Order order = orderRepository
                .findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + orderId));

        /*
         * Idempotency:
         * Order đã CONFIRMED thì coi như đã xử lý thành công.
         */
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            return true;
        }

        /*
         * Quan trọng:
         * VNPay callback đến trễ sau khi order đã hết hạn.
         *
         * Không được:
         * CANCELLED -> CONFIRMED
         *
         * Payment vẫn sẽ được ghi nhận PAID ở module payment.
         */
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return false;
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ValidationException(
                    "Order is not waiting for online payment");
        }

        OrderStatus oldStatus = order.getStatus();

        OrderStateMachine.validateTransition(
                oldStatus,
                OrderStatus.CONFIRMED,
                true,
                0);

        order.setStatus(OrderStatus.CONFIRMED);

        orderRepository.save(order);

        orderStatusHistoryRepository.save(
                OrderStatusHistory.builder()
                        .order(order)
                        .fromStatus(oldStatus)
                        .toStatus(OrderStatus.CONFIRMED)
                        .changedAt(Instant.now())
                        .changedByUserId(null)
                        .build());

        return true;
    }
}