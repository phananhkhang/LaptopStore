package org.akira.laptopstore.order.service;

import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.order.entity.OrderItem;
import org.akira.laptopstore.order.entity.OrderStatusHistory;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.akira.laptopstore.order.mapper.OrderMapper;
import org.akira.laptopstore.order.repository.OrderItemRepository;
import org.akira.laptopstore.order.repository.OrderRepository;
import org.akira.laptopstore.order.repository.OrderStatusHistoryRepository;
import org.akira.laptopstore.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orders;
    @Mock private OrderItemRepository orderItems;
    @Mock private OrderStatusHistoryRepository histories;

    @Test
    void ownerCanReadDetailWithStoredItemAndStatusSnapshots() {
        Order order = order(10L, 42L);
        when(orders.findById(10L)).thenReturn(Optional.of(order));
        when(orderItems.findByOrderId(10L)).thenReturn(List.of(OrderItem.builder()
                .id(20L).productId(2L).variantId(3L).productName("Laptop")
                .variantName("16GB / 512GB").sku("LP-3").unitPrice(new BigDecimal("12500000"))
                .quantity(2).build()));
        when(histories.findByOrderIdOrderByChangedAtDesc(10L)).thenReturn(List.of(OrderStatusHistory.builder()
                .id(30L).toStatus(OrderStatus.PENDING).changedAt(Instant.parse("2026-10-01T10:00:00Z"))
                .changedByUserId(42L).build()));

        var response = service().getOwnOrder(42L, 10L);

        assertEquals("ORD-10", response.orderCode());
        assertEquals(new BigDecimal("25000000"), response.items().getFirst().lineTotal());
        assertEquals(OrderStatus.PENDING, response.statusHistories().getFirst().toStatus());
    }

    @Test
    void userCannotReadAnotherUsersOrder() {
        when(orders.findById(10L)).thenReturn(Optional.of(order(10L, 99L)));

        assertThrows(ResourceNotFoundException.class, () -> service().getOwnOrder(42L, 10L));
    }

    @Test
    void orderListsContainTheQuantityAcrossAllLines() {
        Order order = order(10L, 42L);
        when(orders.findByUserId(42L, PageRequest.of(0, 20))).thenReturn(new PageImpl<>(List.of(order)));
        when(orderItems.findByOrderId(10L)).thenReturn(List.of(
                OrderItem.builder().quantity(2).build(), OrderItem.builder().quantity(1).build()));

        var result = service().getOwnOrders(42L, PageRequest.of(0, 20));

        assertEquals(3, result.getContent().getFirst().totalItems());
    }

    private OrderServiceImpl service() {
        return new OrderServiceImpl(orders, orderItems, histories, new OrderMapper());
    }

    private Order order(Long id, Long userId) {
        return Order.builder().id(id).userId(userId).orderCode("ORD-" + id).status(OrderStatus.PENDING)
                .subtotal(new BigDecimal("25000000")).discountAmount(BigDecimal.ZERO).shippingFee(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("25000000")).recipientName("Nguyen Van A").recipientPhone("0900000000")
                .shippingAddress("Ho Chi Minh City").createdAt(Instant.parse("2026-10-01T10:00:00Z"))
                .updatedAt(Instant.parse("2026-10-01T10:00:00Z")).build();
    }
}
