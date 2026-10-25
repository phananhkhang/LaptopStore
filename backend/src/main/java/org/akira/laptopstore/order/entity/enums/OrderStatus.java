package org.akira.laptopstore.order.entity.enums;

public enum OrderStatus {
    CREATED,                // Khởi tạo đơn
    PENDING_PAYMENT,        // Chờ thanh toán online (VNPAY)
    WAITING_CONFIRMATION,   // Chờ xác nhận (COD)
    CONFIRMED,              // Đã xác nhận đơn hàng
    PACKED,                 // Kho đã đóng gói xong
    SHIPPING,               // Đang giao hàng
    DELIVERED,              // Giao hàng thành công
    DELIVERY_FAILED,        // Giao hàng thất bại
    CANCELLED,              // Đã hủy đơn
    RETURNED                // Đã trả hàng / hoàn tiền
}
