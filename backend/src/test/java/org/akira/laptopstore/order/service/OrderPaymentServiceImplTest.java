package org.akira.laptopstore.order.service;

import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.order.entity.enums.CheckoutPaymentMethod;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.akira.laptopstore.order.repository.OrderRepository;
import org.akira.laptopstore.order.repository.OrderStatusHistoryRepository;
import org.akira.laptopstore.order.service.impl.OrderPaymentServiceImpl;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderPaymentServiceImplTest {
    OrderRepository orders = mock(OrderRepository.class);
    OrderStatusHistoryRepository histories = mock(OrderStatusHistoryRepository.class);
    OrderPaymentService service = new OrderPaymentServiceImpl(orders, histories);
    Order order;

    @BeforeEach void setup() {
        order = Order.builder().id(1L).userId(42L).status(OrderStatus.PENDING_PAYMENT)
                .checkoutPaymentMethod(CheckoutPaymentMethod.VNPAY).totalAmount(BigDecimal.TEN).build();
        when(orders.findByIdForUpdate(1L)).thenReturn(Optional.of(order));
    }
    @Test void verifiedPaymentConfirmsOnce() {
        assertTrue(service.recordVerifiedPayment(1L));
        assertTrue(service.recordVerifiedPayment(1L));
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(histories, times(1)).save(any());
    }
    @Test void latePaymentNeverRevivesCancelledOrder() {
        order.setStatus(OrderStatus.CANCELLED);
        assertFalse(service.recordVerifiedPayment(1L));
        verifyNoInteractions(histories);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }
    @Test void rejectsWrongOwnerAndMethod() {
        assertThrows(ResourceNotFoundException.class, () -> service.lockPayableOrder(1L, 99L, CheckoutPaymentMethod.VNPAY));
        assertThrows(ValidationException.class, () -> service.lockPayableOrder(1L, 42L, CheckoutPaymentMethod.COD));
    }
    @Test void cannotStartPaymentAfterCancellation() {
        order.setStatus(OrderStatus.CANCELLED);
        assertThrows(ValidationException.class, () -> service.lockPayableOrder(1L, 42L, CheckoutPaymentMethod.VNPAY));
    }
}
