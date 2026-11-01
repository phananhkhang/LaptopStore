// frontend/src/modules/catalog/api/catalogApi.ts

import axiosClient from '@/services/api/axiosClient';
import { PageResponse } from '@/types/common';
import {
    Brand,
    Category,
    ProductResponse,
    ProductSearchCriteria,
    ProductSummaryResponse
} from '../types/catalog';

export const catalogApi = {
    // Lấy danh sách thương hiệu (chỉ những brand đang active)
    getBrands: (): Promise<Brand[]> => {
        return axiosClient.get('/brands');
    },

    // Lấy danh sách danh mục (chỉ những category đang active)
    getCategories: (): Promise<Category[]> => {
        return axiosClient.get('/categories');
    },

    // Tìm kiếm, lọc và phân trang sản phẩm
    searchProducts: (params: ProductSearchCriteria): Promise<PageResponse<ProductSummaryResponse>> => {
        return axiosClient.get('/products/search', {
            params: params // Axios sẽ tự động map object này thành query string trên URL (?keyword=...&page=...)
        });
    },

    // Lấy chi tiết sản phẩm theo ID
    getProductById: (id: number): Promise<ProductResponse> => {
        return axiosClient.get(`/products/${id}`);
    },

    // Toàn bộ sản phẩm kèm biến thể/tồn kho (dùng cho màn kho hàng)
    getAllProducts: (): Promise<ProductResponse[]> => {
        return axiosClient.get('/products');
    }
};