package org.akira.laptopstore.payment.service;

import java.util.Map;

import org.akira.laptopstore.payment.dto.response.VnPayIpnResponse;
import org.akira.laptopstore.payment.dto.response.VnPayReturnResponse;

public interface VnPayCallbackService {

        /**
         * Xử lý IPN do server VNPay gửi về.
         *
         * Kiểm tra chữ ký, merchant, transaction reference và số tiền.
         * Sau đó cập nhật trạng thái PaymentTransaction và Payment.
         *
         * Đây là nguồn xác nhận chính thức trạng thái thanh toán VNPay.
         */
        VnPayIpnResponse handleIpn(Map<String, String> params);

        /**
         * Kiểm tra dữ liệu khi VNPay redirect trình duyệt người dùng về hệ thống.
         *
         * Chỉ dùng để hiển thị kết quả cho người dùng.
         * Không dùng return URL để quyết định Payment = PAID.
         */
        VnPayReturnResponse inspectReturn(Map<String, String> params);
}