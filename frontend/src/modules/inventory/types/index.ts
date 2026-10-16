export interface StockMovement {
    id: number;
    variantId: number;
    sku: string;
    productName: string;
    variantName: string;
    type: string;
    delta: number;
    quantityBefore: number;
    quantityAfter: number;
    referenceType?: string;
    referenceId?: string;
    operationKey: string;
    note?: string;
    createdByUserId?: number;
    createdAt: string;
}

export interface InventoryStock {
    variantId: number;
    productId: number;
    productName: string;
    variantName: string;
    sku: string;
    ram: string;
    storage: string;
    color: string;
    price: number;
    quantity: number;
    status: string;
    active: boolean;
    lowStock: boolean;
}

export interface AdjustStockRequest {
    delta: number;
    reason: string;
}

// Cấu trúc phân trang chung trả về từ Spring Data (Page<T>)
export interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}