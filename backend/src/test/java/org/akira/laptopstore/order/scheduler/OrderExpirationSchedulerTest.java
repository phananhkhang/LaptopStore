package org.akira.laptopstore.order.scheduler;

import org.akira.laptopstore.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderExpirationSchedulerTest {

    @Mock
    private OrderService orderService;

    private OrderExpirationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new OrderExpirationScheduler(orderService);
    }

    @Test
    @DisplayName("Scheduler gọi expirePendingOrders trên OrderService")
    void schedulerTriggersOrderService() {
        when(orderService.expirePendingOrders()).thenReturn(3);

        scheduler.scanAndExpirePendingOrders();

        verify(orderService).expirePendingOrders();
    }
}
