package org.akira.laptopstore.inventory.service;

import org.akira.laptopstore.inventory.dto.response.InventoryStockResponse;
import java.util.List;

/**
 * Cung cấp các hợp đồng truy vấn dữ liệu kho hàng (Tồn kho hiện tại, cảnh báo hết hàng).
 */
public interface InventoryQueryService {

    // Lấy thông tin kho của 1 sản phẩm cụ thể
    InventoryStockResponse getStockByVariantId(Long variantId, int threshold);

    // Lấy toàn bộ danh sách tồn kho
    List<InventoryStockResponse> getAllStocks(int threshold);

    // Lấy danh sách sản phẩm sắp hết hàng (cảnh báo low-stock)
    List<InventoryStockResponse> getLowStockVariants(int threshold);
}