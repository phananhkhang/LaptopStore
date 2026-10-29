import { apiClient } from '@/services/api/apiClient';
import { Coupon, CouponUsage, CreateCouponRequest, UpdateCouponRequest } from '../types';

// API dành riêng cho ADMIN (AdminCouponController, CouponUsageController)
export const couponApi = {
    getAllCoupons: async (): Promise<Coupon[]> => {
        const response = await apiClient.get<Coupon[]>('/api/v1/admin/coupons');
        return response.data;
    },

    getCouponById: async (id: number): Promise<Coupon> => {
        const response = await apiClient.get<Coupon>(`/api/v1/admin/coupons/${id}`);
        return response.data;
    },

    createCoupon: async (data: CreateCouponRequest): Promise<Coupon> => {
        const response = await apiClient.post<Coupon>('/api/v1/admin/coupons', data);
        return response.data;
    },

    updateCoupon: async (id: number, data: UpdateCouponRequest): Promise<Coupon> => {
        const response = await apiClient.put<Coupon>(`/api/v1/admin/coupons/${id}`, data);
        return response.data;
    },

    setActive: async (id: number, active: boolean): Promise<Coupon> => {
        const response = await apiClient.patch<Coupon>(`/api/v1/admin/coupons/${id}/active`, { active });
        return response.data;
    },

    getUsagesByCoupon: async (couponId: number): Promise<CouponUsage[]> => {
        const response = await apiClient.get<CouponUsage[]>(`/api/v1/admin/coupon-usages/coupon/${couponId}`);
        return response.data;
    },

    releaseUsage: async (usageId: number): Promise<void> => {
        await apiClient.put(`/api/v1/admin/coupon-usages/${usageId}/release`);
    }
};
