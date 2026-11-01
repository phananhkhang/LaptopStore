import { EmptyState, formatDateTime, formatMoney, Icon, ORDER_STATUS, OrderStatusBadge, Spinner } from '@/components/ui';
import { AccountLayout } from '@/modules/user/components/AccountLayout';
import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { orderApi } from '../../api/orderApi';
import { OrderSummary } from '../../types';

const FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'pending', label: 'Chờ xử lý', match: ['CREATED', 'PENDING_PAYMENT', 'WAITING_CONFIRMATION'] },
  { value: 'active', label: 'Đang giao', match: ['CONFIRMED', 'PACKING', 'PACKED', 'SHIPPING'] },
  { value: 'done', label: 'Đã giao', match: ['DELIVERED'] },
  { value: 'cancelled', label: 'Đã hủy', match: ['CANCELLED', 'RETURNED', 'DELIVERY_FAILED'] },
];

export const OrderHistoryPage = () => {
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('');

  useEffect(() => {
    orderApi.getMine()
      .then((page) => setOrders(page.content))
      .catch(() => setError('Không thể tải lịch sử đơn hàng.'))
      .finally(() => setLoading(false));
  }, []);

  const visible = useMemo(() => {
    const f = FILTERS.find((x) => x.value === filter);
    return f?.match ? orders.filter((o) => f.match!.includes(o.status)) : orders;
  }, [orders, filter]);

  return (
    <AccountLayout title="Đơn hàng của tôi" sub="Theo dõi trạng thái và lịch sử mua sắm của bạn.">
      {loading ? <Spinner /> : error ? (
        <EmptyState icon="alert" title="Không tải được đơn hàng" copy={error} />
      ) : orders.length === 0 ? (
        <EmptyState icon="orders" title="Bạn chưa có đơn hàng nào" copy="Những đơn hàng bạn đặt sẽ xuất hiện tại đây để tiện theo dõi." action={{ label: 'Bắt đầu mua sắm', to: '/products' }} />
      ) : (
        <>
          <div className="chips" style={{ marginTop: 0 }}>
            {FILTERS.map((f) => (
              <button key={f.value} type="button" className={`chip ${filter === f.value ? 'active' : ''}`} onClick={() => setFilter(f.value)}>{f.label}</button>
            ))}
          </div>
          <div className="stack" style={{ gap: 14 }}>
            {visible.length === 0 && <p className="muted small">Không có đơn nào ở trạng thái này.</p>}
            {visible.map((order) => (
              <article key={order.id} className="panel order-card">
                <div className="row between wrap">
                  <div>
                    <strong>#{order.orderCode}</strong>
                    <p className="small muted">{formatDateTime(order.createdAt)} · {order.recipientName}</p>
                  </div>
                  <OrderStatusBadge status={order.status} />
                </div>
                <div className="order-actions">
                  <span className="muted small">{order.totalItems} sản phẩm · {ORDER_STATUS[order.status]?.label ?? order.status}</span>
                  <div className="row">
                    <strong style={{ color: 'var(--orange)', fontSize: 16 }}>{formatMoney(order.totalAmount)}</strong>
                    <Link to={`/orders/${order.id}`} className="btn sm">Xem chi tiết <Icon name="arrow-right" /></Link>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </>
      )}
    </AccountLayout>
  );
};
