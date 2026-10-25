package org.akira.laptopstore.payment.controller;

import java.util.Map;

import org.akira.laptopstore.payment.dto.response.VnPayIpnResponse;
import org.akira.laptopstore.payment.dto.response.VnPayReturnResponse;
import org.akira.laptopstore.payment.service.VnPayCallbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controller tiếp nhận callback từ VNPay.
 *
 * Bao gồm:
 * - IPN: VNPay server gọi trực tiếp về backend.
 * - Return URL: trình duyệt user được redirect về sau thanh toán.
 */
@RestController
@RequestMapping("/api/v1/payments/vnpay")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final VnPayCallbackService callbackService;

    /**
     * Nhận IPN từ VNPay để xác nhận kết quả thanh toán.
     *
     * Đây là luồng server-to-server và là nguồn đáng tin cậy
     * để cập nhật Payment / PaymentTransaction.
     *
     * Nếu xảy ra lỗi hệ thống vẫn trả response theo format VNPay yêu cầu.
     */
    @GetMapping("/ipn")
    public ResponseEntity<VnPayIpnResponse> handleIpn(
            @RequestParam Map<String, String> params) {

        try {
            VnPayIpnResponse response = callbackService.handleIpn(params);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {

            return ResponseEntity.ok(
                    VnPayIpnResponse.systemError());
        }
    }

    /**
     * Xử lý URL mà VNPay redirect trình duyệt user về.
     *
     * Dùng để kiểm tra và hiển thị kết quả thanh toán cho frontend.
     * Không dùng Return URL làm nguồn chính để cập nhật Payment = PAID.
     */
    @GetMapping("/return")
    public ResponseEntity<VnPayReturnResponse> handleReturn(
            @RequestParam Map<String, String> params) {

        VnPayReturnResponse response = callbackService.inspectReturn(params);

        return ResponseEntity.ok(response);
    }
}