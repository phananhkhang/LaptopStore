import { apiClient } from '@/services/api/apiClient';
import { Supplier, CreateSupplierRequest, UpdateSupplierRequest } from '../types';

export const supplierApi = {
    // API dành cho STAFF & ADMIN
    getAllSuppliers: async (): Promise<Supplier[]> => {
        const response = await apiClient.get<Supplier[]>('/api/v1/staff/suppliers');
        return response.data;
    },

    getSupplierById: async (id: number): Promise<Supplier> => {
        const response = await apiClient.get<Supplier>(`/api/v1/staff/suppliers/${id}`);
        return response.data;
    },

    // API dành riêng cho ADMIN
    createSupplier: async (data: CreateSupplierRequest): Promise<Supplier> => {
        const response = await apiClient.post<Supplier>('/api/v1/admin/suppliers', data);
        return response.data;
    },

    updateSupplier: async (id: number, data: UpdateSupplierRequest): Promise<Supplier> => {
        const response = await apiClient.put<Supplier>(`/api/v1/admin/suppliers/${id}`, data);
        return response.data;
    },

    toggleSupplierStatus: async (id: number, status: string): Promise<string> => {
        const response = await apiClient.patch<string>(`/api/v1/admin/suppliers/${id}/status`, null, {
            params: { status }
        });
        return response.data;
    }
};