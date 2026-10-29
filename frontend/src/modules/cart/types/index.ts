export interface CartItem {
  cartItemId: number;
  productId?: number;
  availableQuantity?: number;
  sellable?: boolean;
  variantId: number;
  productName: string;
  sku: string;
  variantName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface Cart {
  id: number | null;
  items: CartItem[];
  totalItems: number;
  totalAmount: number;
}
