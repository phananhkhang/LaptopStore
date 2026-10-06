import { apiClient } from '@/services/api/apiClient';
import { Cart } from '../types';

export const cartApi = {
  get: async (): Promise<Cart> => (await apiClient.get<Cart>('/me/cart')).data,

  updateItem: async (cartItemId: number, quantity: number): Promise<Cart> => (
    await apiClient.patch<Cart>(`/me/cart/items/${cartItemId}`, { quantity })
  ).data,

  removeItem: async (cartItemId: number): Promise<void> => {
    await apiClient.delete(`/me/cart/items/${cartItemId}`);
  },
};
