import { apiClient } from '@/services/api/apiClient';
import {
    PurchaseOrder,
    CreatePurchaseOrderRequest,
    ReceivePurchaseOrderRequest
} from '../types';

export const purchaseOrderApi = {
    getAllPurchaseOrders: async (): Promise<PurchaseOrder[]> => {
        const response = await apiClient.get<PurchaseOrder[]>('/api/v1/staff/purchase-orders');
        return response.data;
    },

    getPurchaseOrderById: async (id: number): Promise<PurchaseOrder> => {
        const response = await apiClient.get<PurchaseOrder>(`/api/v1/staff/purchase-orders/${id}`);
        return response.data;
    },

    createPurchaseOrder: async (data: CreatePurchaseOrderRequest): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>('/api/v1/staff/purchase-orders', data);
        return response.data;
    },

    receivePurchaseOrder: async (id: number, data: ReceivePurchaseOrderRequest): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(`/api/v1/staff/purchase-orders/${id}/receive`, data);
        return response.data;
    },

    cancelPurchaseOrder: async (id: number): Promise<PurchaseOrder> => {
        const response = await apiClient.post<PurchaseOrder>(`/api/v1/staff/purchase-orders/${id}/cancel`);
        return response.data;
    }
};