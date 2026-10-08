import { useEffect, useState } from 'react';
import { orderApi } from '../../api/orderApi';
import { OrderSummary } from '../../types';

export const StaffOrderListPage = () => {
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    orderApi.getAllForStaff().then((page) => setOrders(page.content)).catch(() => setError('Không thể tải danh sách đơn hàng.'));
  }, []);

  return (
    <main style={{ maxWidth: 960, margin: '0 auto', padding: '2rem' }}>
      <h1>Quản lý đơn hàng</h1>
      {error && <p role="alert" style={{ color: '#b42318' }}>{error}</p>}
      {!error && orders.length === 0 && <p>Chưa có đơn hàng.</p>}
      {orders.map((order) => <p key={order.id}>{order.orderCode} · {order.recipientName} · {order.status}</p>)}
    </main>
  );
};
