package org.akira.laptopstore.inventory.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.inventory.entity.StockMovement;
import org.akira.laptopstore.inventory.repository.StockMovementRepository;
import org.akira.laptopstore.inventory.service.InventoryService;
import org.akira.laptopstore.inventory.dto.request.OrderStockDeduction;
import org.akira.laptopstore.catalog.dto.response.StockChangeResult;
import org.akira.laptopstore.catalog.service.CatalogCheckoutService;
import org.akira.laptopstore.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final StockMovementRepository stockMovementRepository;
    private final CatalogCheckoutService catalogCheckoutService;

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

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void deductForOrder(Long orderId, Long actorId, List<OrderStockDeduction> lines) {
        validateDeductionRequest(orderId, lines);

        for (OrderStockDeduction line : sortedByVariant(lines)) {
            validateLine(line);

            String operationKey = operationKey(orderId, line.getVariantId());
            if (alreadyDeducted(operationKey)) {
                continue;
            }

            StockChangeResult stockChange = catalogCheckoutService.deductStock(
                    line.getVariantId(), line.getQuantity());
            stockMovementRepository.save(createSaleMovement(
                    orderId, actorId, line, operationKey, stockChange));
        }
    }

    private void validateDeductionRequest(Long orderId, List<OrderStockDeduction> lines) {
        if (orderId == null || orderId <= 0 || lines == null || lines.isEmpty()) {
            throw new ValidationException("An order and at least one stock line are required");
        }
    }

    private List<OrderStockDeduction> sortedByVariant(List<OrderStockDeduction> lines) {
        return lines.stream()
                .sorted(Comparator.comparing(OrderStockDeduction::getVariantId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private void validateLine(OrderStockDeduction line) {
        if (line == null || line.getVariantId() == null
                || line.getQuantity() == null || line.getQuantity() <= 0) {
            throw new ValidationException("Stock lines must contain a positive variantId and quantity");
        }
    }

    private boolean alreadyDeducted(String operationKey) {
        return stockMovementRepository.findByOperationKey(operationKey).isPresent();
    }

    private String operationKey(Long orderId, Long variantId) {
        return "order-" + orderId + "-variant-" + variantId;
    }

    private StockMovement createSaleMovement(Long orderId, Long actorId, OrderStockDeduction line,
                                             String operationKey, StockChangeResult stockChange) {
        return StockMovement.builder()
                .variantId(line.getVariantId())
                .type("SALE")
                .delta(-line.getQuantity())
                .quantityBefore(stockChange.getQuantityBefore())
                .quantityAfter(stockChange.getQuantityAfter())
                .referenceType("ORDER")
                .referenceId(orderId.toString())
                .operationKey(operationKey)
                .createdByUserId(actorId)
                .build();
    }

    @Override
    @Transactional // Quan trọng: Bao bọc transaction cho cả thao tác cập nhật Catalog và ghi log
    public void adjustStock(Long variantId, Integer delta, String reason, Long actorId) {
        if (delta == null || delta == 0) {
            throw new ValidationException("Delta phải khác 0");
        }

        // 1. Gọi sang Catalog để cập nhật quantity (CatalogCheckoutService)
        StockChangeResult stockChange = catalogCheckoutService.adjustStock(variantId, delta);

        // 2. Ghi nhận log (Ledger)
        String operationKey = "adj-" + variantId + "-" + System.currentTimeMillis();
        StockMovement movement = StockMovement.builder()
                .variantId(variantId)
                .type(delta > 0 ? "IN" : "OUT")
                .delta(delta)
                .quantityBefore(stockChange.getQuantityBefore())
                .quantityAfter(stockChange.getQuantityAfter())
                .referenceType("MANUAL_ADJUSTMENT")
                .referenceId("N/A")
                .operationKey(operationKey)
                .note(reason)
                .createdByUserId(actorId)
                .build();

        stockMovementRepository.save(movement);
    }
}
