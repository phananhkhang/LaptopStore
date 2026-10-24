import { apiClient } from '@/services/api/apiClient';
import { PageResponse, ProductSummaryResponse, ProductResponse } from '../types';

export const productApi = {
    // Gọi API tìm kiếm, lọc và phân trang sản phẩm
    searchProducts: async (params?: Record<string, any>): Promise<PageResponse<ProductSummaryResponse>> => {
        const response = await apiClient.get<PageResponse<ProductSummaryResponse>>('/api/v1/products/search', { params });
        return response.data;
    },

    // Gọi API lấy chi tiết một sản phẩm theo ID
    getProductById: async (id: number): Promise<ProductResponse> => {
        const response = await apiClient.get<ProductResponse>(`/api/v1/products/${id}`);
        return response.data;
    }
};