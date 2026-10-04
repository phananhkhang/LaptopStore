export interface CartItem {
  cartItemId: number;
  variantId: number;
  productName: string | null;
  sku: string | null;
  variantName: string | null;
  quantity: number;
  unitPrice: number | null;
  lineTotal: number | null;
}

export interface Cart {
  id: number | null;
  items: CartItem[];
  totalItems: number;
  totalAmount: number | null;
}
