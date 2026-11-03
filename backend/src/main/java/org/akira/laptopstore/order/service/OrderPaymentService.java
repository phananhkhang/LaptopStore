package org.akira.laptopstore.order.service;

public interface OrderPaymentService {

    /**
     * Xác nhận thanh toán online cho Order.
     *
     * @return true nếu Order được/đã CONFIRMED.
     *         false nếu Order đã CANCELLED và không được phép hồi sinh.
     */
    boolean confirmOnlinePayment(Long orderId);
}
