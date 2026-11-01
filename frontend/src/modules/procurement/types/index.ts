// ==================== SUPPLIER TYPES ====================
export interface Supplier {
    id: number;
    name: string;
    phone: string;
    email: string;
    address: string;
    status: string; // 'ACTIVE' | 'INACTIVE'
}

export interface CreateSupplierRequest {
    name: string;
    phone: string;
    email: string;
    address: string;
}

export interface UpdateSupplierRequest {
    name: string;
    phone: string;
    email: string;
    address: string;
    status: string;
}

// ==================== PURCHASE ORDER TYPES ====================
export interface PurchaseOrderItem {
    id?: number;
    variantId: number;
    quantity: number;
    receivedQuantity: number;
    unitCost: number;
}

export interface PurchaseOrder {
    id: number;
    supplier: Supplier;
    createdByUserId: number;
    status: string; // 'DRAFT' | 'PARTIALLY_RECEIVED' | 'RECEIVED' | 'CANCELLED'
    note?: string;
    createdAt: string;
    receivedAt?: string;
    items: PurchaseOrderItem[];
}

export interface CreatePurchaseOrderItemRequest {
    variantId: number;
    quantity: number;
    unitCost: number;
}

export interface CreatePurchaseOrderRequest {
    supplierId: number;
    note?: string;
    items: CreatePurchaseOrderItemRequest[];
}

export interface PurchaseStockReceipt {
    variantId: number;
    quantity: number;
}

export interface ReceivePurchaseOrderRequest {
    lines: PurchaseStockReceipt[];
    // Backend bắt buộc (chống double-submit); sinh mới cho mỗi lần mở hộp thoại nhận hàng.
    idempotencyKey: string;
}