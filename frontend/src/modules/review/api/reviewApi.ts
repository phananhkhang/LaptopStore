import { apiClient } from '@/services/api/apiClient';
import { CreateReviewRequest, Page, Review, ReviewSummary, UpdateReviewRequest } from '../types';

export const reviewApi = {
    // API công khai
    getProductReviews: async (productId: number, page = 0, size = 10): Promise<Page<Review>> => {
        const response = await apiClient.get<Page<Review>>(`/api/v1/products/${productId}/reviews`, { params: { page, size } });
        return response.data;
    },

    getProductReviewSummary: async (productId: number): Promise<ReviewSummary> => {
        const response = await apiClient.get<ReviewSummary>(`/api/v1/products/${productId}/reviews/summary`);
        return response.data;
    },

    // API dành cho USER
    createReview: async (data: CreateReviewRequest): Promise<Review> => {
        const response = await apiClient.post<Review>('/api/v1/me/reviews', data);
        return response.data;
    },

    updateReview: async (reviewId: number, data: UpdateReviewRequest): Promise<Review> => {
        const response = await apiClient.put<Review>(`/api/v1/me/reviews/${reviewId}`, data);
        return response.data;
    },

    getMyReviews: async (page = 0, size = 10): Promise<Page<Review>> => {
        const response = await apiClient.get<Page<Review>>('/api/v1/me/reviews', { params: { page, size } });
        return response.data;
    },

    // API dành cho STAFF & ADMIN
    getStaffReviews: async (params: { visible?: boolean; rating?: number; page?: number; size?: number }): Promise<Page<Review>> => {
        const response = await apiClient.get<Page<Review>>('/api/v1/staff/reviews', { params });
        return response.data;
    },

    updateVisibility: async (reviewId: number, visible: boolean): Promise<Review> => {
        const response = await apiClient.patch<Review>(`/api/v1/staff/reviews/${reviewId}/visibility`, { visible });
        return response.data;
    }
};
