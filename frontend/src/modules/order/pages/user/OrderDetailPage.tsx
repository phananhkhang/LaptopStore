import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { orderApi } from '../../api/orderApi';
import { OrderDetail } from '../../types';

const money = (amount: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);

export const OrderDetailPage = () => {
  const { orderId } = useParams();
  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!orderId) return;
    orderApi.getMineById(orderId).then(setOrder).catch(() => setError('Không tìm thấy đơn hàng hoặc bạn không có quyền xem đơn này.'));
  }, [orderId]);

  if (error) return <main style={{ padding: '2rem' }}><p role="alert">{error}</p><Link to="/orders">Quay lại đơn hàng</Link></main>;
  if (!order) return <main style={{ padding: '2rem' }}>Đang tải đơn hàng...</main>;

  return (
    <main style={{ maxWidth: 960, margin: '0 auto', padding: '2rem' }}>
      <h1>Đơn {order.orderCode}</h1>
      <p>Trạng thái: <strong>{order.status}</strong></p>
      <p>Người nhận: {order.recipientName} · {order.recipientPhone}</p>
      <p>Địa chỉ: {order.shippingAddress}</p>
      <section>
        <h2>Sản phẩm</h2>
        {order.items.map((item) => <p key={item.id}>{item.productName} — {item.quantity} × {money(item.unitPrice)} = {money(item.lineTotal)}</p>)}
      </section>
      <p><strong>Tổng thanh toán: {money(order.totalAmount)}</strong></p>
      <section>
        <h2>Lịch sử trạng thái</h2>
        {order.statusHistories.map((history) => <p key={history.id}>{history.toStatus} · {new Date(history.changedAt).toLocaleString('vi-VN')}</p>)}
      </section>
      <Link to="/orders">Quay lại đơn hàng</Link>
    </main>
  );
};
