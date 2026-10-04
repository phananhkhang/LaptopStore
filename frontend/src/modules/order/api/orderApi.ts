import { apiClient } from '@/services/api/apiClient';
import { OrderDetail, OrderSummary, PageResponse } from '../types';

export const orderApi = {
  getMine: async (): Promise<PageResponse<OrderSummary>> => (
    await apiClient.get<PageResponse<OrderSummary>>('/me/orders')
  ).data,

  getMineById: async (orderId: string): Promise<OrderDetail> => (
    await apiClient.get<OrderDetail>(`/me/orders/${orderId}`)
  ).data,

  getAllForStaff: async (): Promise<PageResponse<OrderSummary>> => (
    await apiClient.get<PageResponse<OrderSummary>>('/staff/orders')
  ).data,
};
