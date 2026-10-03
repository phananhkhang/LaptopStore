package org.akira.laptopstore.payment.controller;

import org.akira.laptopstore.payment.dto.request.RecordCodPaymentRequest;
import org.akira.laptopstore.payment.dto.request.StartPaymentRequest;
import org.akira.laptopstore.payment.dto.response.PaymentResponse;
import org.akira.laptopstore.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> startPayment(
            @RequestBody StartPaymentRequest request) {

        PaymentResponse response = paymentService.startPayment(request);

        return ResponseEntity
                .status(201)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                paymentService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getByOrder(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                paymentService.getByOrderId(orderId));
    }

    @PutMapping("/cod")
    public ResponseEntity<PaymentResponse> recordCod(
            @RequestBody RecordCodPaymentRequest request) {

        return ResponseEntity.ok(
                paymentService.recordCodPayment(request));
    }
}
