import { apiClient } from '@/services/api/apiClient';
import { Cart } from '../types';

export const cartApi = {
  get: async (): Promise<Cart> => (await apiClient.get<Cart>('/me/cart')).data,

  addItem: async (productVariantId: number, quantity = 1): Promise<Cart> => (
    await apiClient.post<Cart>('/me/cart/items', { productVariantId, quantity })
  ).data,

  updateItem: async (cartItemId: number, quantity: number): Promise<Cart> => (
    await apiClient.patch<Cart>(`/me/cart/items/${cartItemId}`, { quantity })
  ).data,

  removeItem: async (cartItemId: number): Promise<void> => {
    await apiClient.delete(`/me/cart/items/${cartItemId}`);
  },
};
