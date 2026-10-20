package org.akira.laptopstore.inventory.controller;

import lombok.RequiredArgsConstructor;

import org.akira.laptopstore.security.principal.CustomUserDetails;

import org.akira.laptopstore.inventory.service.InventoryQueryService;
import org.akira.laptopstore.inventory.service.InventoryService;

import org.akira.laptopstore.inventory.dto.request.AdjustStockRequest;

import org.akira.laptopstore.inventory.dto.response.InventoryStockResponse;
import org.akira.laptopstore.inventory.dto.response.StockMovementResponse;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryQueryService inventoryQueryService;

    // 1. Xem lịch sử biến động kho toàn hệ thống (STAFF / ADMIN)
    @GetMapping("/staff/inventory/movements")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<Page<StockMovementResponse>> getAllMovements(Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getAllMovements(pageable));
    }

    // 2. Xem lịch sử biến động kho của 1 biến thể (STAFF / ADMIN)
    @GetMapping("/staff/inventory/variants/{variantId}/history")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<Page<StockMovementResponse>> getVariantHistory(
            @PathVariable Long variantId, Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getHistoryByVariantId(variantId, pageable));
    }

    // 3. Xem danh sách sản phẩm sắp hết hàng (STAFF / ADMIN)
    @GetMapping("/staff/inventory/low-stock")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<InventoryStockResponse>> getLowStockVariants(
            @RequestParam(defaultValue = "5") int threshold) {
        return ResponseEntity.ok(inventoryQueryService.getLowStockVariants(threshold));
    }

    // 4. Điều chỉnh tồn kho thủ công (Chỉ ADMIN)
    @PostMapping("/admin/inventory/variants/{variantId}/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adjustStock(
            @PathVariable Long variantId,
            @Valid @RequestBody AdjustStockRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Xóa hardcode, lấy trực tiếp ID của người đang thực hiện request từ token
        Long actorId = userDetails.getId();

        inventoryService.adjustStock(variantId, request.getDelta(), request.getReason(), actorId);
        return ResponseEntity.ok("Điều chỉnh tồn kho thành công!");
    }

}