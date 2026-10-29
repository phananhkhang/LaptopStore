import { apiClient } from "@/services/api/apiClient";
import {
  CheckoutPreviewRequest,
  CheckoutPreviewResponse,
  CreateOrderRequest,
  OrderDetail,
  OrderSummary,
  PageResponse,
  OrderStatus,
} from "../types";

export const orderApi = {
  getMine: async (params?: {
    page?: number;
    size?: number;
    status?: string;
    search?: string;
  }): Promise<PageResponse<OrderSummary>> =>
    (await apiClient.get<PageResponse<OrderSummary>>("/me/orders", { params }))
      .data,

  getMineById: async (orderId: string): Promise<OrderDetail> =>
    (await apiClient.get<OrderDetail>(`/me/orders/${orderId}`)).data,

  getAllForStaff: async (params?: {
    page?: number;
    size?: number;
    status?: string;
    search?: string;
  }): Promise<PageResponse<OrderSummary>> =>
    (
      await apiClient.get<PageResponse<OrderSummary>>("/staff/orders", {
        params,
      })
    ).data,

  getStaffById: async (id: string): Promise<OrderDetail> =>
    (await apiClient.get(`/staff/orders/${id}`)).data,
  updateStatus: async (id: number, status: OrderStatus): Promise<OrderDetail> =>
    (await apiClient.put(`/staff/orders/${id}/status`, { status })).data,
  cancel: async (id: number): Promise<OrderDetail> =>
    (await apiClient.post(`/me/orders/${id}/cancel`)).data,

  previewCheckout: async (
    payload: CheckoutPreviewRequest,
  ): Promise<CheckoutPreviewResponse> =>
    (
      await apiClient.post<CheckoutPreviewResponse>(
        "/me/orders/preview",
        payload,
      )
    ).data,

  createOrder: async (
    payload: CreateOrderRequest,
    idempotencyKey?: string,
  ): Promise<OrderDetail> =>
    (
      await apiClient.post<OrderDetail>("/me/orders", payload, {
        headers: idempotencyKey
          ? { "Idempotency-Key": idempotencyKey }
          : undefined,
      })
    ).data,
};
