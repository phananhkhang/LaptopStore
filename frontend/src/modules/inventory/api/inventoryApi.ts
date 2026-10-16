import {apiClient} from '../../../services/api/apiClient';
import { StockMovement, InventoryStock, AdjustStockRequest, PageResponse } from '../types';

export const inventoryApi = {
    // 1. Xem lịch sử biến động kho toàn hệ thống
    getAllMovements: (page: number = 0, size: number = 20) =>
        apiClient.get<PageResponse<StockMovement>>(`/api/v1/staff/inventory/movements?page=${page}&size=${size}`),

    // 2. Xem lịch sử biến động kho của 1 biến thể
    getVariantHistory: (variantId: number, page: number = 0, size: number = 20) =>
        apiClient.get<PageResponse<StockMovement>>(`/api/v1/staff/inventory/variants/${variantId}/history?page=${page}&size=${size}`),

    // 3. Xem danh sách sản phẩm sắp hết hàng
    getLowStockVariants: (threshold: number = 5) =>
        apiClient.get<InventoryStock[]>(`/api/v1/staff/inventory/low-stock?threshold=${threshold}`),

    // 4. Điều chỉnh tồn kho thủ công (Chỉ ADMIN)
    adjustStock: (variantId: number, data: AdjustStockRequest) =>
        apiClient.post<string>(`/api/v1/admin/inventory/variants/${variantId}/adjust`, data),
};