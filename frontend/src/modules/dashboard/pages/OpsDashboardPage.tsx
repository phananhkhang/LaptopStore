import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '@/app/providers/AuthProvider';
import { formatDateTime, formatMoney, Icon, IconName, OrderStatusBadge, PageHeading } from '@/components/ui';
import { ROLES } from '@/constants/roles';
import { catalogApi } from '@/modules/catalog/api/catalogApi';
import { inventoryApi } from '@/modules/inventory/api/inventoryApi';
import { InventoryStock } from '@/modules/inventory/types';
import { orderApi } from '@/modules/order/api/orderApi';
import { OrderSummary } from '@/modules/order/types';

const PENDING = ['CREATED', 'PENDING_PAYMENT', 'WAITING_CONFIRMATION'];
const IN_PROGRESS = ['CONFIRMED', 'PACKING', 'PACKED', 'SHIPPING'];

const Metric: React.FC<{ label: string; value: React.ReactNode; sub: string; icon: IconName; tone?: '' | 'warn' | 'bad' }> = ({ label, value, sub, icon, tone = '' }) => (
  <div className="metric">
    <div className="metric-label">{label}<Icon name={icon} /></div>
    <div className="metric-value">{value}</div>
    <small className={tone}>{sub}</small>
  </div>
);

/** Tổng quan Staff/Admin: số liệu lấy trực tiếp từ API đơn hàng, kho và catalog. */
export const OpsDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const isAdmin = !!user?.roles.includes(ROLES.ADMIN);
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [totalOrders, setTotalOrders] = useState<number | null>(null);
  const [lowStock, setLowStock] = useState<InventoryStock[] | null>(null);
  const [productCount, setProductCount] = useState<number | null>(null);

  useEffect(() => {
    orderApi.getAllForStaff().then((p) => { setOrders(p.content); setTotalOrders(p.totalElements); }).catch(() => setTotalOrders(null));
    inventoryApi.getLowStockVariants(5).then((r) => setLowStock(r.data)).catch(() => setLowStock(null));
    catalogApi.searchProducts({ page: 0, size: 1 }).then((p) => setProductCount(p.totalElements)).catch(() => setProductCount(null));
  }, []);

  const pending = orders.filter((o) => PENDING.includes(o.status)).length;
  const inProgress = orders.filter((o) => IN_PROGRESS.includes(o.status)).length;
  const delivered = orders.filter((o) => o.status === 'DELIVERED');
  const revenue = delivered.reduce((s, o) => s + o.totalAmount, 0);

  return (
    <>
      <PageHeading
        eyebrow={isAdmin ? 'Quản trị / Tổng quan' : 'Vận hành / Tổng quan'}
        title={`Chào ${user?.fullName?.split(' ').pop() || 'bạn'}, một ngày hiệu quả nhé.`}
        sub="Số liệu được cập nhật từ hệ thống mỗi lần mở trang."
        actions={<>
          <Link to="/staff/orders" className="btn sm"><Icon name="orders" /> Xử lý đơn</Link>
          <Link to="/staff/procurement" className="btn sm primary"><Icon name="plus" /> Lập phiếu nhập</Link>
        </>}
      />
      <div className="metrics">
        <Metric label="Chờ xác nhận" value={totalOrders === null ? '—' : pending} sub="Đơn mới cần xử lý" icon="clock" tone={pending ? 'warn' : ''} />
        <Metric label="Đang xử lý / giao" value={totalOrders === null ? '—' : inProgress} sub="Đã xác nhận, đang chuẩn bị" icon="truck" />
        <Metric label="SKU tồn thấp (≤ 5)" value={lowStock === null ? '—' : lowStock.length} sub="Chủ động lập phiếu nhập" icon="box" tone={lowStock?.length ? 'bad' : ''} />
        <Metric
          label={isAdmin ? 'Doanh thu đơn đã giao' : 'Sản phẩm đang bán'}
          value={isAdmin ? formatMoney(revenue) : productCount ?? '—'}
          sub={isAdmin ? `${delivered.length} đơn đã giao` : 'Trong danh mục cửa hàng'}
          icon={isAdmin ? 'chart' : 'laptop'}
        />
      </div>

      <div className="ops-cols">
        <section className="panel">
          <div className="row between" style={{ marginBottom: 14 }}>
            <h3>Đơn hàng gần đây</h3>
            <Link to="/staff/orders" className="text-btn small">Xem tất cả <Icon name="arrow-right" /></Link>
          </div>
          <div className="table-wrap" style={{ border: 0 }}>
            <table className="data-table">
              <thead><tr><th>Mã đơn</th><th>Khách hàng</th><th>Giá trị</th><th>Trạng thái</th></tr></thead>
              <tbody>
                {orders.length === 0 ? (
                  <tr><td colSpan={4} className="table-empty">{totalOrders === null ? 'Không tải được đơn hàng.' : 'Chưa có đơn hàng.'}</td></tr>
                ) : orders.slice(0, 6).map((o) => (
                  <tr key={o.id}>
                    <td><div className="primary-cell">#{o.orderCode}</div><div className="subcell">{formatDateTime(o.createdAt)}</div></td>
                    <td>{o.recipientName}</td>
                    <td>{formatMoney(o.totalAmount)}</td>
                    <td><OrderStatusBadge status={o.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
        <section className="panel">
          <div className="row between" style={{ marginBottom: 6 }}>
            <h3>Sắp hết hàng</h3>
            <Link to="/staff/inventory?low=1" className="text-btn small">Kho hàng <Icon name="arrow-right" /></Link>
          </div>
          {lowStock === null ? <p className="small muted">Không tải được dữ liệu kho.</p> : lowStock.length === 0 ? (
            <p className="small muted" style={{ marginTop: 10 }}>Tồn kho đang ổn định.</p>
          ) : lowStock.slice(0, 6).map((s) => (
            <div key={s.variantId} className="row between" style={{ padding: '12px 0', borderBottom: '1px solid var(--line)' }}>
              <div style={{ minWidth: 0 }}>
                <strong className="small" style={{ display: 'block' }}>{s.productName}</strong>
                <span className="small muted">{s.sku}</span>
              </div>
              <span className={`badge ${s.quantity === 0 ? 'red' : 'orange'}`}>{s.quantity} máy</span>
            </div>
          ))}
        </section>
      </div>
    </>
  );
};
