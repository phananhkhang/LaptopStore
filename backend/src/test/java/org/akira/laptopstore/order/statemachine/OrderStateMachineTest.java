package org.akira.laptopstore.order.statemachine;

import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStateMachineTest {

    private OrderStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new OrderStateMachine();
    }

    @Test
    @DisplayName("Staff chuyển đơn từ CONFIRMED sang PACKING rồi SHIPPING thành công")
    void validHappyFlowTransitions() {
        assertDoesNotThrow(() -> stateMachine.validateTransition(OrderStatus.CONFIRMED, OrderStatus.PACKING, true, 0));
        assertDoesNotThrow(() -> stateMachine.validateTransition(OrderStatus.PACKING, OrderStatus.SHIPPING, true, 0));
        assertDoesNotThrow(() -> stateMachine.validateTransition(OrderStatus.SHIPPING, OrderStatus.DELIVERED, true, 0));
    }

    @Test
    @DisplayName("Chặn nhảy cóc trạng thái (CONFIRMED không thể nhảy thẳng lên DELIVERED)")
    void invalidJumpTransition() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                stateMachine.validateTransition(OrderStatus.CONFIRMED, OrderStatus.DELIVERED, true, 0)
        );
        assertTrue(ex.getMessage().contains("Không thể chuyển trạng thái"));
    }

    @Test
    @DisplayName("Khách hàng có thể hủy đơn khi ở WAITING_CONFIRMATION")
    void userCanCancelAtWaitingConfirmation() {
        assertDoesNotThrow(() -> stateMachine.validateTransition(OrderStatus.WAITING_CONFIRMATION, OrderStatus.CANCELLED, false, 0));
    }

    @Test
    @DisplayName("Khách hàng không thể tự hủy đơn khi đơn đã PACKING")
    void userCannotCancelWhenPacked() {
        assertThrows(ValidationException.class, () ->
                stateMachine.validateTransition(OrderStatus.PACKING, OrderStatus.CANCELLED, false, 0)
        );
    }

    @Test
    @DisplayName("Giao lại thành công nếu chưa vượt quá 3 lần")
    void retryDeliveryUnderLimit() {
        assertDoesNotThrow(() -> stateMachine.validateTransition(OrderStatus.DELIVERY_FAILED, OrderStatus.SHIPPING, true, 2));
    }

    @Test
    @DisplayName("Chặn giao lại nếu đã giao thất bại đủ 3 lần")
    void retryDeliveryExceedLimit() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                stateMachine.validateTransition(OrderStatus.DELIVERY_FAILED, OrderStatus.SHIPPING, true, 3)
        );
        assertTrue(ex.getMessage().contains("đã giao thất bại 3 lần"));
    }
}
