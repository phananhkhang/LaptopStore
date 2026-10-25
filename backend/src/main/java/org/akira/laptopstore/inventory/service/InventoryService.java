package org.akira.laptopstore.inventory.service;

import org.akira.laptopstore.inventory.dto.request.OrderStockDeduction;
import org.akira.laptopstore.inventory.dto.request.PurchaseStockReceipt;
import org.akira.laptopstore.inventory.dto.response.StockMovementResponse;

import org.akira.laptopstore.inventory.entity.StockMovement;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface InventoryService {
    List<StockMovementResponse> getHistoryByVariantId(Long variantId);

    // Bổ sung hàm phân trang cho 1 biến thể
    Page<StockMovementResponse> getHistoryByVariantId(Long variantId, Pageable pageable);

    // Bổ sung hàm xem toàn bộ lịch sử biến động kho toàn hệ thống (có phân trang)
    Page<StockMovementResponse> getAllMovements(Pageable pageable);

    StockMovement recordMovement(StockMovement movement);

    void deductForOrder(Long orderId, Long actorId, List<OrderStockDeduction> lines);

    // BỔ SUNG HÀM HOÀN KHO CHO ĐƠN HÀNG BỊ HỦY
    void restoreForOrder(Long orderId, Long actorId, List<OrderStockDeduction> lines);

    void adjustStock(Long variantId, Integer delta, String reason, Long actorId);

    // Hợp đồng cho module procurement gọi sang khi xác nhận nhận hàng
    void receivePurchase(Long purchaseOrderId, Long actorId, List<PurchaseStockReceipt> lines);
}
