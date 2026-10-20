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

        @PutMapping("/cod")
        @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
        public ResponseEntity<PaymentResponse> recordCod(
                        @Valid @RequestBody RecordCodPaymentRequest request) {

                return ResponseEntity.ok(
                                paymentService.recordCodPayment(request));
        }

        @PostMapping("/{paymentId}/vnpay-session")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<PaymentSessionResponse> createVnpaySession(
                        @AuthenticationPrincipal CustomUserDetails userDetails,

                        @PathVariable Long paymentId,

                        HttpServletRequest request) {

                PaymentSessionResponse response = paymentService
                                .createVnpaySession(
                                                userDetails.getId(),
                                                paymentId,
                                                request.getRemoteAddr());

                return ResponseEntity.ok(response);
        }
}
