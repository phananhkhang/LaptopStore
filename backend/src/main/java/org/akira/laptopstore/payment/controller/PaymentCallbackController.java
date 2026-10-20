package org.akira.laptopstore.payment.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments/vnpay")
@RequiredArgsConstructor
public class PaymentCallbackController {

    @GetMapping("/ipn")
    public ResponseEntity<?> handleIpn(
            @RequestParam Map<String, String> params) {

        // xử lý sau

        return ResponseEntity.ok().build();
    }
}
