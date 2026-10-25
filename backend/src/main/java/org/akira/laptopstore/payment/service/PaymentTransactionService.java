package org.akira.laptopstore.payment.service;

import java.util.List;

import org.akira.laptopstore.payment.dto.response.PaymentTransactionResponse;
import org.akira.laptopstore.payment.entity.Payment;
import org.akira.laptopstore.payment.enums.PaymentProvider;

public interface PaymentTransactionService {

    /**
     * Lấy chi tiết một lần giao dịch thanh toán.
     * Kiểm tra user phải là chủ sở hữu đơn hàng tương ứng.
     */
    PaymentTransactionResponse getById(Long userId, Long id);

    /**
     * Lấy toàn bộ các lần thử thanh toán của một Payment.
     * Một Payment có thể có nhiều PaymentTransaction khi thanh toán lại.
     */
    List<PaymentTransactionResponse> getByPaymentId(
            Long userId,
            Long paymentId);

    /**
     * Đánh dấu giao dịch thanh toán thành công.
     * Transaction chuyển SUCCESS và Payment tương ứng chuyển PAID.
     */
    PaymentTransactionResponse markSuccess(Long transactionId);

    /**
     * Đánh dấu giao dịch thanh toán thất bại.
     * Transaction chuyển FAILED và Payment chuyển FAILED
     * nếu Payment chưa được thanh toán thành công.
     */
    PaymentTransactionResponse markFailed(Long transactionId);

    /**
     * Tạo một lần thử thanh toán mới ở trạng thái PENDING.
     * Lưu provider, mã tham chiếu và số tiền của giao dịch.
     */
    PaymentTransactionResponse createPendingTransaction(
            Long paymentId,
            PaymentProvider provider,
            Long providerTransactionId,
            String providerReference);
}