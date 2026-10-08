import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { orderApi } from '../../api/orderApi';
import { OrderSummary } from '../../types';

const money = (amount: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);

export const OrderHistoryPage = () => {
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    orderApi.getMine().then((page) => setOrders(page.content)).catch(() => setError('Không thể tải lịch sử đơn hàng.'));
  }, []);

  return (
    <main style={{ maxWidth: 960, margin: '0 auto', padding: '2rem' }}>
      <h1>Đơn hàng của tôi</h1>
      {error && <p role="alert" style={{ color: '#b42318' }}>{error}</p>}
      {!error && orders.length === 0 && <p>Bạn chưa có đơn hàng nào.</p>}
      {orders.map((order) => (
        <article key={order.id} style={{ borderBottom: '1px solid #ddd', padding: '1rem 0' }}>
          <strong>{order.orderCode}</strong>
          <p>{order.status} · {order.totalItems} sản phẩm · {money(order.totalAmount)}</p>
          <Link to={`/orders/${order.id}`}>Xem chi tiết</Link>
        </article>
      ))}
    </main>
  );
};
