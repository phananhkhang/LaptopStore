import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { cartApi } from '@/modules/cart/api/cartApi';
import { Cart } from '@/modules/cart/types';
import { useAuth } from './AuthProvider';

interface CartContextValue {
  cart: Cart | null;
  /** Tổng số lượng sản phẩm trong giỏ (badge trên header). */
  count: number;
  refresh: () => Promise<void>;
  addItem: (variantId: number, quantity?: number) => Promise<Cart>;
  /** Cập nhật state sau khi trang giỏ hàng đổi số lượng/xóa. */
  setCart: (cart: Cart | null) => void;
}

const CartContext = createContext<CartContextValue | undefined>(undefined);

/** Giỏ hàng lưu ở backend (/me/cart); provider chỉ đồng bộ để header hiển thị số lượng. */
export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState<Cart | null>(null);

  const refresh = useCallback(async () => {
    if (!isAuthenticated) {
      setCart(null);
      return;
    }
    try {
      setCart(await cartApi.get());
    } catch {
      setCart(null);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const addItem = useCallback(async (variantId: number, quantity = 1) => {
    const updated = await cartApi.addItem(variantId, quantity);
    setCart(updated);
    return updated;
  }, []);

  const count = cart?.totalItems ?? cart?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0;

  return (
    <CartContext.Provider value={{ cart, count, refresh, addItem, setCart }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used within CartProvider');
  return ctx;
};
