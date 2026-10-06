package org.akira.laptopstore.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.inventory.entity.StockMovement;
import org.akira.laptopstore.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // API lấy lịch sử kho của 1 variant
    @GetMapping("/variants/{variantId}/history")
    public ResponseEntity<List<StockMovement>> getVariantHistory(@PathVariable Long variantId) {
        return ResponseEntity.ok(inventoryService.getHistoryByVariantId(variantId));
    }
}