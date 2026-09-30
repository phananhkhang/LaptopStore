package org.akira.laptopstore.inventory.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.inventory.entity.StockMovement;
import org.akira.laptopstore.inventory.repository.StockMovementRepository;
import org.akira.laptopstore.inventory.service.InventoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final StockMovementRepository stockMovementRepository;

    @Override
    public List<StockMovement> getHistoryByVariantId(Long variantId) {
        return stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(variantId);
    }

    @Override
    public StockMovement recordMovement(StockMovement movement) {
        // Kiểm tra chống lặp bằng operation key trước khi lưu
        if (stockMovementRepository.findByOperationKey(movement.getOperationKey()).isPresent()) {
            throw new RuntimeException("Biến động kho đã được ghi nhận trước đó!");
        }
        return stockMovementRepository.save(movement);
    }
}