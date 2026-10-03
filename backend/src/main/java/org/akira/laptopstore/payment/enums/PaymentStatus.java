package org.akira.laptopstore.payment.enums;

public enum PaymentStatus {
    // đang chờ thanh toán
    PENDING,
    // đã thanh toán
    PAID,
    // thanh toán thất bại
    FAILED,
    // payment bị hủy
    CANCELLED
}