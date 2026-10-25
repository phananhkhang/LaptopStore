import { apiClient } from '@/services/api/apiClient';
import { Brand, CreateBrandRequest, UpdateBrandRequest } from '../types';

export const brandApi = {
    getAllBrands: async (): Promise<Brand[]> => {
        const response = await apiClient.get<Brand[]>('/api/v1/brands');
        return response.data;
    },

    getBrandById: async (id: number): Promise<Brand> => {
        const response = await apiClient.get<Brand>(`/api/v1/brands/${id}`);
        return response.data;
    },

    createBrand: async (data: CreateBrandRequest): Promise<Brand> => {
        const response = await apiClient.post<Brand>('/api/v1/admin/brands', data);
        return response.data;
    },

    updateBrand: async (id: number, data: UpdateBrandRequest): Promise<Brand> => {
        const response = await apiClient.put<Brand>(`/api/v1/admin/brands/${id}`, data);
        return response.data;
    },

    deleteBrand: async (id: number): Promise<string> => {
        const response = await apiClient.delete<string>(`/api/v1/admin/brands/${id}`);
        return response.data;
    },
};