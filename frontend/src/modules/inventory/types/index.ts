export interface StockMovement {
    id: number;
    variantId: number;
    type: string;
    delta: number;
    quantityBefore: number;
    quantityAfter: number;
    referenceType?: string;
    referenceId?: string;
    operationKey: string;
    note?: string;
    createdAt: string;
}