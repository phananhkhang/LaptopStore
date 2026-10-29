package org.akira.laptopstore.payment.controller;

import org.akira.laptopstore.payment.dto.request.RecordCodPaymentRequest;
import org.akira.laptopstore.payment.dto.request.StartPaymentRequest;
import org.akira.laptopstore.payment.dto.response.PaymentResponse;
import org.akira.laptopstore.payment.dto.response.PaymentSessionResponse;
import org.akira.laptopstore.payment.service.PaymentService;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

        private final PaymentService paymentService;

        @GetMapping("/staff/order/{orderId}")
        @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
        public ResponseEntity<PaymentResponse> getForStaff(@PathVariable Long orderId) {
                return ResponseEntity.ok(paymentService.getForStaff(orderId));
        }

        /**
         * Khởi tạo thanh toán cho một đơn hàng của user hiện tại.
         *
         * User gửi orderId và phương thức thanh toán.
         * Controller lấy userId từ tài khoản đang đăng nhập
         * và chuyển dữ liệu cho PaymentService xử lý.
         *
         * @return Payment vừa được tạo với HTTP 201.
         */
        @PostMapping
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<PaymentResponse> startPayment(
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        @Valid @RequestBody StartPaymentRequest request) {

                PaymentResponse response = paymentService.startPayment(
                                userDetails.getId(),
                                request);

                return ResponseEntity.status(201).body(response);
        }

        /**
         * Lấy thông tin một Payment theo paymentId.
         *
         * Chỉ USER sở hữu đơn hàng liên quan mới được xem.
         */
        @GetMapping("/{id}")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<PaymentResponse> getById(
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                paymentService.getById(
                                                userDetails.getId(),
                                                id));
        }

        /**
         * Lấy thông tin thanh toán của một đơn hàng theo orderId.
         *
         * Dùng khi frontend cần kiểm tra đơn hàng đã thanh toán
         * hay đang chờ thanh toán.
         */
        @GetMapping("/order/{orderId}")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<PaymentResponse> getByOrder(
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        @PathVariable Long orderId) {

                return ResponseEntity.ok(
                                paymentService.getByOrderId(
                                                userDetails.getId(),
                                                orderId));
        }

        /**
         * Xác nhận đã thu tiền đối với thanh toán COD.
         *
         * Chỉ STAFF hoặc ADMIN được thực hiện,
         * thường được gọi sau khi đơn hàng đã giao thành công.
         */
        @PutMapping("/cod")
        @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
        public ResponseEntity<PaymentResponse> recordCod(
                        @Valid @RequestBody RecordCodPaymentRequest request) {

                return ResponseEntity.ok(
                                paymentService.recordCodPayment(request));
        }

        /**
         * Tạo phiên thanh toán VNPay cho Payment.
         *
         * Backend tạo PaymentTransaction và URL thanh toán,
         * sau đó frontend dùng URL này để redirect user sang VNPay.
         */
        @PostMapping("/{paymentId}/vnpay-session")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<PaymentSessionResponse> createVnpaySession(
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        @PathVariable Long paymentId,
                        HttpServletRequest request) {

                PaymentSessionResponse response = paymentService.createVnpaySession(
                                userDetails.getId(),
                                paymentId,
                                request.getRemoteAddr());

                return ResponseEntity.ok(response);
        }
}
