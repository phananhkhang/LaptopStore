import { EmptyState, formatDateTime, formatMoney, Icon, ORDER_STATUS, OrderStatusBadge, ProductImage, Spinner } from '@/components/ui';
import { useSkuProducts } from '@/modules/cart/hooks/useSkuProducts';
import { AccountLayout } from '@/modules/user/components/AccountLayout';
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { orderApi } from '../../api/orderApi';
import { OrderDetail } from '../../types';

const STAGES = ['WAITING_CONFIRMATION', 'CONFIRMED', 'PACKING', 'SHIPPING', 'DELIVERED'];
const stageOf = (status: string) => {
  if (status === 'PACKED') return STAGES.indexOf('PACKING');
  if (['CREATED', 'PENDING_PAYMENT'].includes(status)) return 0;
  return STAGES.indexOf(status);
};

export const OrderDetailPage = () => {
  const { orderId } = useParams();
  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  const products = useSkuProducts(order?.items.map((i) => i.sku) ?? []);

  useEffect(() => {
    if (!orderId) return;
    orderApi.getMineById(orderId).then(setOrder).catch(() => setError('Không tìm thấy đơn hàng hoặc bạn không có quyền xem đơn này.'));
  }, [orderId]);

  if (error) {
    return (
      <AccountLayout title="Chi tiết đơn hàng">
        <EmptyState icon="search" title="Không tìm thấy đơn hàng" copy={error} action={{ label: 'Quay lại đơn hàng', to: '/orders' }} />
      </AccountLayout>
    );
  }
  if (!order) return <AccountLayout title="Chi tiết đơn hàng"><Spinner /></AccountLayout>;

  const current = stageOf(order.status);
  const interrupted = current < 0;
  const historyDate = (status: string) => {
    const h = order.statusHistories.find((x) => x.toStatus === status || (status === 'PACKING' && x.toStatus === 'PACKED'));
    return h ? formatDateTime(h.changedAt) : 'Tiếp theo';
  };

  return (
    <AccountLayout
      title={`Đơn #${order.orderCode}`}
      sub={`Đặt lúc ${formatDateTime(order.createdAt)}`}
      actions={<Link to="/orders" className="btn sm"><Icon name="arrow-left" /> Tất cả đơn hàng</Link>}
    >
      <div className="stack">
        <section className="panel">
          <div className="row between wrap">
            <h3>Hành trình đơn hàng</h3>
            <OrderStatusBadge status={order.status} />
          </div>
          {interrupted ? (
            <div className="notice red" style={{ marginTop: 16 }}>
              <Icon name="alert" />
              <div><strong>{ORDER_STATUS[order.status]?.label ?? order.status}</strong><p>Cập nhật lúc {formatDateTime(order.updatedAt)}.</p></div>
            </div>
          ) : (
            <div className="timeline">
              {STAGES.map((s, i) => (
                <div key={s} className={`timeline-step ${i < current ? 'done' : i === current ? 'current' : ''}`}>
                  <div className="step-dot">{i < current ? '✓' : i + 1}</div>
                  <strong>{ORDER_STATUS[s].label}</strong>
                  <small>{historyDate(s)}</small>
                </div>
              ))}
            </div>
          )}
        </section>

        <div className="grid2">
          <section className="panel">
            <h3 className="panel-title">Sản phẩm</h3>
            {order.items.map((item) => {
              const info = products[item.sku];
              return (
                <div className="order-line" key={item.id}>
                  <ProductImage src={info?.thumbnailImage} alt={item.productName} hint={item.productName} />
                  <div style={{ minWidth: 0 }}>
                    <Link to={`/products/${item.productId}`}><strong>{item.productName}</strong></Link>
                    <p className="small muted">{item.variantName} · SL {item.quantity}</p>
                  </div>
                  <strong className="push">{formatMoney(item.lineTotal)}</strong>
                </div>
              );
            })}
          </section>
          <section className="panel">
            <h3 className="panel-title">Giao hàng & thanh toán</h3>
            <p><strong>{order.recipientName}</strong> · {order.recipientPhone}</p>
            <p className="small muted" style={{ marginTop: 4 }}>{order.shippingAddress}</p>
            <hr className="divider" />
            <div className="summary-line"><span className="muted">Tạm tính</span><strong>{formatMoney(order.subtotal)}</strong></div>
            <div className="summary-line"><span className="muted">Giảm giá{order.couponCodeSnapshot ? ` (${order.couponCodeSnapshot})` : ''}</span><strong style={{ color: 'var(--green)' }}>−{formatMoney(order.discountAmount)}</strong></div>
            <div className="summary-line"><span className="muted">Vận chuyển</span><strong>{order.shippingFee ? formatMoney(order.shippingFee) : 'Miễn phí'}</strong></div>
            <div className="summary-line total"><span>Tổng cộng</span><strong>{formatMoney(order.totalAmount)}</strong></div>
          </section>
        </div>
      </div>
    </AccountLayout>
  );
};
