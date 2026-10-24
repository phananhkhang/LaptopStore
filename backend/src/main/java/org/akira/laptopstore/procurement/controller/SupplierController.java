package org.akira.laptopstore.procurement.controller;

import org.akira.laptopstore.procurement.dto.request.CreateSupplierRequest;
import org.akira.laptopstore.procurement.dto.request.UpdateSupplierRequest;
import org.akira.laptopstore.procurement.dto.response.SupplierResponse;
import org.akira.laptopstore.procurement.service.SupplierService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;

    // ==========================================
    // API DÀNH CHO STAFF & ADMIN (Chỉ đọc)
    // ==========================================

    @GetMapping("/api/v1/staff/suppliers")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/api/v1/staff/suppliers/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    // ==========================================
    // API DÀNH RIÊNG CHO ADMIN (Thêm/Sửa/Đổi trạng thái)
    // ==========================================

    @PostMapping("/api/v1/admin/suppliers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }

    @PutMapping("/api/v1/admin/suppliers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @PatchMapping("/api/v1/admin/suppliers/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> toggleSupplierStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        supplierService.toggleSupplierStatus(id, status);
        return ResponseEntity.ok("Cập nhật trạng thái nhà cung cấp thành công!");
    }
}
