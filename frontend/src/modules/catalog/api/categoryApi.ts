import { apiClient } from '@/services/api/apiClient';
import { Category, CreateCategoryRequest, UpdateCategoryRequest } from '../types';

export const categoryApi = {
  getAllCategories: async (): Promise<Category[]> => {
    const response = await apiClient.get<Category[]>('/api/v1/categories');
    return response.data;
  },

  getCategoryById: async (id: number): Promise<Category> => {
    const response = await apiClient.get<Category>(`/api/v1/categories/${id}`);
    return response.data;
  },

  createCategory: async (data: CreateCategoryRequest): Promise<Category> => {
    const response = await apiClient.post<Category>('/api/v1/admin/categories', data);
    return response.data;
  },

  updateCategory: async (id: number, data: UpdateCategoryRequest): Promise<Category> => {
    const response = await apiClient.put<Category>(`/api/v1/admin/categories/${id}`, data);
    return response.data;
  },

  deleteCategory: async (id: number): Promise<string> => {
    const response = await apiClient.delete<string>(`/api/v1/admin/categories/${id}`);
    return response.data;
  },
};
