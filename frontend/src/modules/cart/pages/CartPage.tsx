import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { cartApi } from '../api/cartApi';
import { Cart } from '../types';

const money = (amount: number | null) => amount === null
  ? 'Sẽ xác định khi checkout'
  : new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);

export const CartPage = () => {
  const [cart, setCart] = useState<Cart | null>(null);
  const [error, setError] = useState<string | null>(null);

  const loadCart = useCallback(async () => {
    try {
      setError(null);
      setCart(await cartApi.get());
    } catch {
      setError('Không thể tải giỏ hàng. Hãy thử lại sau.');
    }
  }, []);

  useEffect(() => { void loadCart(); }, [loadCart]);

  const updateQuantity = async (cartItemId: number, quantity: number) => {
    if (quantity < 1) return;
    try {
      setCart(await cartApi.updateItem(cartItemId, quantity));
    } catch {
      setError('Không thể cập nhật số lượng.');
    }
  };

  const removeItem = async (cartItemId: number) => {
    try {
      await cartApi.removeItem(cartItemId);
      await loadCart();
    } catch {
      setError('Không thể xóa sản phẩm khỏi giỏ.');
    }
  };

  if (!cart && !error) return <main style={{ padding: '2rem' }}>Đang tải giỏ hàng...</main>;

  return (
    <main style={{ maxWidth: 960, margin: '0 auto', padding: '2rem' }}>
      <h1>Giỏ hàng</h1>
      {error && <p role="alert" style={{ color: '#b42318' }}>{error}</p>}
      {cart?.items.length === 0 && <p>Giỏ hàng đang trống.</p>}
      {cart?.items.map((item) => (
        <article key={item.cartItemId} style={{ borderBottom: '1px solid #ddd', padding: '1rem 0' }}>
          <h2 style={{ fontSize: '1.1rem', margin: 0 }}>{item.productName ?? `Sản phẩm #${item.variantId}`}</h2>
          <p>{item.variantName ?? `Biến thể #${item.variantId}`}</p>
          <label>
            Số lượng{' '}
            <input
              aria-label={`Số lượng sản phẩm ${item.variantId}`}
              type="number"
              min="1"
              value={item.quantity}
              onChange={(event) => void updateQuantity(item.cartItemId, Number(event.target.value))}
              style={{ width: 64 }}
            />
          </label>
          <p>Thành tiền: {money(item.lineTotal)}</p>
          <button type="button" onClick={() => void removeItem(item.cartItemId)}>Xóa</button>
        </article>
      ))}
      {cart && cart.items.length > 0 && (
        <section style={{ marginTop: '1.5rem' }}>
          <p><strong>Tạm tính:</strong> {money(cart.totalAmount)}</p>
          <Link to="/checkout">Tiếp tục checkout</Link>
        </section>
      )}
    </main>
  );
};
