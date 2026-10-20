package org.akira.laptopstore.inventory.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.catalog.dto.response.VariantStockSnapshot;
import org.akira.laptopstore.catalog.service.CatalogVariantQueryService;
import org.akira.laptopstore.inventory.dto.response.InventoryStockResponse;
import org.akira.laptopstore.inventory.service.InventoryQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryQueryServiceImpl implements InventoryQueryService {

    // Gọi chéo sang module catalog thông qua Contract hợp lệ
    private final CatalogVariantQueryService catalogVariantQueryService;

    @Override
    public InventoryStockResponse getStockByVariantId(Long variantId, int threshold) {
        VariantStockSnapshot snapshot = catalogVariantQueryService.getVariantStock(variantId);
        return mapToResponse(snapshot, threshold);
    }

    @Override
    public List<InventoryStockResponse> getAllStocks(int threshold) {
        return catalogVariantQueryService.getAllVariantStocks().stream()
                .map(snapshot -> mapToResponse(snapshot, threshold))
                .toList();
    }

    @Override
    public List<InventoryStockResponse> getLowStockVariants(int threshold) {
        return catalogVariantQueryService.getLowStockVariants(threshold).stream()
                .map(snapshot -> mapToResponse(snapshot, threshold))
                .toList();
    }

    /**
     * Hàm nội bộ: Chuyển đổi từ VariantStockSnapshot (Catalog) sang InventoryStockResponse (Inventory)
     * và tự động tính toán cờ cảnh báo lowStock.
     */
    private InventoryStockResponse mapToResponse(VariantStockSnapshot snapshot, int threshold) {
        int currentQty = snapshot.getQuantity() == null ? 0 : snapshot.getQuantity();

        return InventoryStockResponse.builder()
                .variantId(snapshot.getVariantId())
                .productId(snapshot.getProductId())
                .productName(snapshot.getProductName())
                .variantName(snapshot.getVariantName())
                .sku(snapshot.getSku())
                .ram(snapshot.getRam())
                .storage(snapshot.getStorage())
                .color(snapshot.getColor())
                .price(snapshot.getPrice())
                .quantity(currentQty)
                .status(snapshot.getStatus())
                .active(snapshot.isActive())
                .lowStock(currentQty <= threshold) // Đánh cờ TRUE nếu tồn kho <= ngưỡng cảnh báo
                .build();
    }
}