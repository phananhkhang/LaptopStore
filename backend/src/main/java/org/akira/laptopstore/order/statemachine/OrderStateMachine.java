package org.akira.laptopstore.order.statemachine;

import org.akira.laptopstore.exception.ValidationException;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Component
public class OrderStateMachine {

    // Ma trận các bước chuyển hợp lệ theo đúng sơ đồ
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.CREATED, Set.of(OrderStatus.PENDING_PAYMENT, OrderStatus.WAITING_CONFIRMATION),
            OrderStatus.PENDING_PAYMENT, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.WAITING_CONFIRMATION, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PACKING, OrderStatus.CANCELLED),
            OrderStatus.PACKING, Set.of(OrderStatus.SHIPPING),
            OrderStatus.SHIPPING, Set.of(OrderStatus.DELIVERED, OrderStatus.DELIVERY_FAILED),
            OrderStatus.DELIVERY_FAILED, Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED),
            OrderStatus.DELIVERED, Set.of(OrderStatus.RETURNED),
            OrderStatus.CANCELLED, Collections.emptySet(),
            OrderStatus.RETURNED, Collections.emptySet()
    );

    /**
     * Kiểm tra tính hợp lệ khi chuyển trạng thái.
     *
     * @param fromStatus        Trạng thái hiện tại
     * @param toStatus          Trạng thái mong muốn chuyển đến
     * @param isStaff           Người thực hiện có quyền STAFF/ADMIN hay không
     * @param deliveryAttempts  Số lần đã thử giao hàng (tính đến thời điểm hiện tại)
     */
    public static void validateTransition(OrderStatus fromStatus, OrderStatus toStatus, boolean isStaff, int deliveryAttempts) {
        if (fromStatus == null || toStatus == null) {
            throw new ValidationException("Trạng thái nguồn và trạng thái đích không được null");
        }

        if (fromStatus == toStatus) {
            throw new ValidationException("Trạng thái mới phải khác trạng thái hiện tại: " + fromStatus);
        }

        Set<OrderStatus> allowedNextStates = VALID_TRANSITIONS.getOrDefault(fromStatus, Collections.emptySet());
        if (!allowedNextStates.contains(toStatus)) {
            throw new ValidationException(String.format(
                    "Không thể chuyển trạng thái đơn hàng từ [%s] sang [%s]", fromStatus, toStatus
            ));
        }

        // Kiểm tra quyền của khách hàng (USER thông thường)
        if (!isStaff) {
            // Khách chỉ được hủy đơn khi đơn ở PENDING_PAYMENT hoặc WAITING_CONFIRMATION
            if (toStatus == OrderStatus.CANCELLED) {
                if (fromStatus != OrderStatus.PENDING_PAYMENT && fromStatus != OrderStatus.WAITING_CONFIRMATION) {
                    throw new ValidationException("Khách hàng chỉ có thể hủy đơn khi chưa đóng gói/giao hàng");
                }
            } else {
                throw new ValidationException("Khách hàng không có quyền chuyển sang trạng thái: " + toStatus);
            }
        }

        // Kiểm tra số lần giao lại (tối đa 3 lần giao thất bại)
        if (fromStatus == OrderStatus.DELIVERY_FAILED && toStatus == OrderStatus.SHIPPING) {
            if (deliveryAttempts >= 3) {
                throw new ValidationException("Đơn hàng đã giao thất bại 3 lần, không thể giao lại. Vui lòng hủy đơn.");
            }
        }
    }
}
