package org.akira.laptopstore.payment.controller;

import java.util.List;

import org.akira.laptopstore.payment.dto.response.PaymentTransactionResponse;
import org.akira.laptopstore.payment.service.PaymentTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment-transactions")
@RequiredArgsConstructor
public class PaymentTransactionController {

    private final PaymentTransactionService paymentTransactionService;

    @GetMapping("/{id}")
    public ResponseEntity<PaymentTransactionResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                paymentTransactionService.getById(id));
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<PaymentTransactionResponse>> getByPaymentId(@PathVariable Long paymentId) {

        return ResponseEntity.ok(
                paymentTransactionService.getByPaymentId(paymentId));
    }
}