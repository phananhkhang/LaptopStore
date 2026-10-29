import { apiClient } from "@/services/api/apiClient";
import { CheckoutPaymentMethod } from "../types";

export interface Payment {
  id: number;
  orderId: number;
  method: CheckoutPaymentMethod;
  amount: number;
  status:
    "PENDING" | "PAID" | "FAILED" | "CANCELLED" | "REQUIRES_RECONCILIATION";
  expiresAt: string | null;
}

export const paymentApi = {
  getForStaff: async (orderId: number): Promise<Payment> =>
    (await apiClient.get(`/payments/staff/order/${orderId}`)).data,
  getForOrder: async (orderId: number): Promise<Payment> =>
    (await apiClient.get<Payment>(`/payments/order/${orderId}`)).data,
  start: async (
    orderId: number,
    method: CheckoutPaymentMethod,
  ): Promise<Payment> =>
    (await apiClient.post<Payment>("/payments", { orderId, method })).data,
  session: async (
    paymentId: number,
  ): Promise<{ paymentUrl: string; expiresAt: string }> =>
    (await apiClient.post(`/payments/${paymentId}/vnpay-session`)).data,
  collectCod: async (orderId: number): Promise<Payment> =>
    (await apiClient.put("/payments/cod", { orderId })).data,
};

export async function openVnpay(orderId: number) {
  const payment = await paymentApi.start(orderId, "VNPAY");
  const session = await paymentApi.session(payment.id);
  const url = new URL(session.paymentUrl);
  if (
    url.protocol !== "https:" ||
    !["vnpayment.vn", "vnpay.vn"].some(
      (host) => url.hostname === host || url.hostname.endsWith(`.${host}`),
    )
  ) {
    throw new Error("Đường dẫn thanh toán không hợp lệ.");
  }
  sessionStorage.setItem("commerce.paymentOrderId", String(orderId));
  window.location.assign(url.href);
}
