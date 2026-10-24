package org.akira.laptopstore.procurement.controller;

import org.akira.laptopstore.procurement.dto.request.CreatePurchaseOrderRequest;
import org.akira.laptopstore.procurement.dto.request.ReceivePurchaseOrderRequest;

import org.akira.laptopstore.procurement.dto.response.PurchaseOrderResponse;

import org.akira.laptopstore.procurement.service.PurchaseOrderService;

import org.akira.laptopstore.security.principal.CustomUserDetails;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/v1/staff/purchase-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class PurchaseOrderController {
private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(
            @Valid @RequestBody CreatePurchaseOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderResponse> receivePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody ReceivePurchaseOrderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        PurchaseOrderResponse response = purchaseOrderService.receivePurchaseOrder(id, userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancelPurchaseOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        PurchaseOrderResponse response = purchaseOrderService.cancelPurchaseOrder(id, userDetails.getId());
        return ResponseEntity.ok(response);
    }
}
