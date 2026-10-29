export type OrderStatus =
  | 'CREATED'
  | 'PENDING_PAYMENT'
  | 'WAITING_CONFIRMATION'
  | 'CONFIRMED'
  | 'PACKING'
  | 'SHIPPING'
  | 'DELIVERED'
  | 'DELIVERY_FAILED'
  | 'CANCELLED'
  | 'RETURNED';

export type CheckoutPaymentMethod = 'COD' | 'VNPAY';

export interface OrderSummary {
  id: number;
  orderCode: string;
  status: OrderStatus;
  totalItems: number;
  totalAmount: number;
  recipientName: string;
  createdAt: string;
}

export interface OrderItem {
  id: number;
  productId: number;
  variantId: number;
  productName: string;
  variantName: string;
  sku: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface OrderStatusHistory {
  id: number;
  fromStatus: OrderStatus | null;
  toStatus: OrderStatus;
  changedAt: string;
  changedBy: number | null;
}

export interface OrderDetail extends Omit<OrderSummary, 'totalItems'> {
  paymentMethod: CheckoutPaymentMethod;
  customerNote: string | null;
  userId: number;
  subtotal: number;
  discountAmount: number;
  shippingFee: number;
  recipientPhone: string;
  shippingAddress: string;
  couponCodeSnapshot: string | null;
  items: OrderItem[];
  statusHistories: OrderStatusHistory[];
  updatedAt: string;
}

export interface CheckoutPreviewRequest {
  addressId?: number | null;
  recipientName?: string;
  recipientPhone?: string;
  shippingAddress?: string;
  paymentMethod?: CheckoutPaymentMethod;
  couponCode?: string;
}

export interface CheckoutPreviewResponse {
  addressId: number | null;
  recipientName: string | null;
  recipientPhone: string | null;
  shippingAddress: string | null;
  paymentMethod: CheckoutPaymentMethod;
  couponCode: string | null;
  subtotal: number;
  discountAmount: number;
  shippingFee: number;
  totalAmount: number;
  totalItems: number;
  items: OrderItem[];
}

export interface CreateOrderRequest {
  addressId?: number | null;
  recipientName?: string;
  recipientPhone?: string;
  shippingAddress?: string;
  paymentMethod?: CheckoutPaymentMethod;
  couponCode?: string;
  note?: string;
  idempotencyKey?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
