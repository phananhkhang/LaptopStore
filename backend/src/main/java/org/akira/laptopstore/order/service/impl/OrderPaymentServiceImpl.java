package org.akira.laptopstore.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.order.entity.OrderStatusHistory;
import org.akira.laptopstore.order.entity.enums.CheckoutPaymentMethod;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.akira.laptopstore.order.repository.OrderRepository;
import org.akira.laptopstore.order.repository.OrderStatusHistoryRepository;
import org.akira.laptopstore.order.service.OrderPaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class OrderPaymentServiceImpl implements OrderPaymentService {
    private final OrderRepository orders;
    private final OrderStatusHistoryRepository histories;

    public BigDecimal lockPayableOrder(Long orderId, Long userId, CheckoutPaymentMethod method) {
        Order order = orders.findByIdForUpdate(orderId).filter(o -> o.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getCheckoutPaymentMethod() != method) throw new ValidationException("Payment method differs from checkout");
        boolean payable = method == CheckoutPaymentMethod.VNPAY
                ? order.getStatus() == OrderStatus.PENDING_PAYMENT
                : order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.RETURNED;
        if (!payable) throw new ValidationException("Order cannot be paid in current status");
        return order.getTotalAmount();
    }

    public boolean recordVerifiedPayment(Long orderId) {
        Order order = orders.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getCheckoutPaymentMethod() != CheckoutPaymentMethod.VNPAY) throw new ValidationException("Order is not online");
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.RETURNED) return false;
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            order.setStatus(OrderStatus.CONFIRMED);
            orders.save(order);
            histories.save(OrderStatusHistory.builder().order(order).fromStatus(OrderStatus.PENDING_PAYMENT)
                    .toStatus(OrderStatus.CONFIRMED).changedAt(Instant.now()).build());
        }
        return true;
    }

    public BigDecimal lockDeliveredCodOrder(Long orderId) {
        Order order = orders.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getCheckoutPaymentMethod() != CheckoutPaymentMethod.COD || order.getStatus() != OrderStatus.DELIVERED) {
            throw new ValidationException("COD can only be collected for a delivered COD order");
        }
        return order.getTotalAmount();
    }
}
