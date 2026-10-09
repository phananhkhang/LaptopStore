package org.akira.laptopstore.inventory.service;

import org.akira.laptopstore.inventory.entity.StockMovement;
import java.util.List;
import org.akira.laptopstore.inventory.dto.request.OrderStockDeduction;

public interface InventoryService {
    List<StockMovement> getHistoryByVariantId(Long variantId);
    StockMovement recordMovement(StockMovement movement);
    void deductForOrder(Long orderId, Long actorId, List<OrderStockDeduction> lines);
}
