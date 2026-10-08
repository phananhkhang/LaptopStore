export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPING' | 'DELIVERED' | 'CANCELLED' | 'RETURNED';

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

export interface OrderDetail extends OrderSummary {
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

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
