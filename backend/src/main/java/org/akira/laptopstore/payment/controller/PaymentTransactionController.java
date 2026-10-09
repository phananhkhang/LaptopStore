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
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/payment-transactions")
@RequiredArgsConstructor
public class PaymentTransactionController {

    private final PaymentTransactionService paymentTransactionService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentTransactionResponse> getById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(paymentTransactionService.getById(userDetails.getId(), id));
    }

    @GetMapping("/payment/{paymentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PaymentTransactionResponse>> getByPaymentId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                paymentTransactionService.getByPaymentId(
                        userDetails.getId(),
                        paymentId));
    }
}