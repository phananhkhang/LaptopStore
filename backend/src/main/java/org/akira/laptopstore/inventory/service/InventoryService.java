package org.akira.laptopstore.inventory.service;

import org.akira.laptopstore.inventory.entity.StockMovement;
import java.util.List;

public interface InventoryService {
    List<StockMovement> getHistoryByVariantId(Long variantId);
    StockMovement recordMovement(StockMovement movement);
}