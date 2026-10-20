import { apiClient } from '@/services/api/apiClient';
import {
  CheckoutPreviewRequest,
  CheckoutPreviewResponse,
  CreateOrderRequest,
  OrderDetail,
  OrderSummary,
  PageResponse,
} from '../types';

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

  previewCheckout: async (payload: CheckoutPreviewRequest): Promise<CheckoutPreviewResponse> => (
    await apiClient.post<CheckoutPreviewResponse>('/me/orders/preview', payload)
  ).data,

  createOrder: async (payload: CreateOrderRequest, idempotencyKey?: string): Promise<OrderDetail> => (
    await apiClient.post<OrderDetail>('/me/orders', payload, {
      headers: idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : undefined,
    })
  ).data,
};
