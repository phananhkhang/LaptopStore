import { apiErrorMessage, formatDateTime, formatMoney, Icon, Modal, ORDER_STATUS, OrderStatusBadge, PageHeading, Pager, Spinner, useToast } from '@/components/ui';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { orderApi } from '../../api/orderApi';
import { OrderDetail, OrderStatus, OrderSummary } from '../../types';

// Khớp OrderStateMachine.VALID_TRANSITIONS ở backend.
const NEXT: Record<string, OrderStatus[]> = {
  CREATED: ['WAITING_CONFIRMATION', 'PENDING_PAYMENT'],
  PENDING_PAYMENT: ['CONFIRMED', 'CANCELLED'],
  WAITING_CONFIRMATION: ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['PACKING', 'CANCELLED'],
  PACKING: ['SHIPPING'],
  SHIPPING: ['DELIVERED', 'DELIVERY_FAILED'],
  DELIVERY_FAILED: ['SHIPPING', 'CANCELLED'],
  DELIVERED: ['RETURNED'],
};

const FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'WAITING_CONFIRMATION', label: 'Chờ xác nhận' },
  { value: 'PENDING_PAYMENT', label: 'Chờ thanh toán' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'PACKING', label: 'Đang chuẩn bị' },
  { value: 'SHIPPING', label: 'Đang giao' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];
const PAGE_SIZE = 20;

export const StaffOrderListPage = () => {
  const toast = useToast();
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState('');
  const [keyword, setKeyword] = useState('');
  const [detail, setDetail] = useState<OrderDetail | null>(null);
  const [pending, setPending] = useState<{ order: OrderSummary; to: OrderStatus } | null>(null);
  const [note, setNote] = useState('');
  const [saving, setSaving] = useState(false);

  const load = useCallback(async (p: number) => {
    try {
      setLoading(true);
      setError(null);
      const res = await orderApi.getAllForStaff({ page: p, size: PAGE_SIZE });
      setOrders(res.content);
      setTotalPages(res.totalPages);
    } catch {
      setError('Không thể tải danh sách đơn hàng.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(page); }, [load, page]);

  const visible = useMemo(() => {
    const q = keyword.trim().toLowerCase();
    return orders.filter((o) => (!filter || o.status === filter || (filter === 'PACKING' && o.status === 'PACKED'))
      && (!q || o.orderCode.toLowerCase().includes(q) || o.recipientName.toLowerCase().includes(q)));
  }, [orders, filter, keyword]);

  const openDetail = async (orderId: number) => {
    try {
      setDetail(await orderApi.getStaffOrderById(orderId));
    } catch (err) {
      toast(apiErrorMessage(err, 'Không tải được chi tiết đơn.'), { tone: 'error' });
    }
  };

  const confirmTransition = async () => {
    if (!pending) return;
    try {
      setSaving(true);
      await orderApi.updateStatus(pending.order.id, pending.to, note.trim() || undefined);
      toast(`Đơn #${pending.order.orderCode}: ${ORDER_STATUS[pending.to]?.label ?? pending.to}.`);
      setPending(null);
      setNote('');
      await load(page);
    } catch (err) {
      toast(apiErrorMessage(err, 'Không thể cập nhật trạng thái đơn.'), { tone: 'error' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <PageHeading
        eyebrow="Vận hành / Đơn hàng"
        title="Đơn hàng cần xử lý"
        sub="Xác nhận, đóng gói và giao hàng theo đúng quy trình."
        actions={<button type="button" className="btn sm" onClick={() => void load(page)}><Icon name="refresh" /> Làm mới</button>}
      />

      <div className="ops-toolbar">
        <label className="search">
          <Icon name="search" />
          <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Tìm mã đơn hoặc tên khách..." aria-label="Tìm đơn hàng" />
        </label>
        <div className="chips" style={{ margin: 0 }}>
          {FILTERS.map((f) => (
            <button key={f.value} type="button" className={`chip ${filter === f.value ? 'active' : ''}`} onClick={() => setFilter(f.value)}>{f.label}</button>
          ))}
        </div>
      </div>

      {loading ? <Spinner /> : (
        <div className="table-wrap">
          <table className="data-table">
            <thead><tr><th>Mã đơn / Thời gian</th><th>Khách hàng</th><th>Số lượng</th><th>Giá trị</th><th>Trạng thái</th><th style={{ textAlign: 'right' }}>Thao tác</th></tr></thead>
            <tbody>
              {error ? (
                <tr><td colSpan={6} className="table-empty">{error}</td></tr>
              ) : visible.length === 0 ? (
                <tr><td colSpan={6} className="table-empty">Không có đơn hàng phù hợp. Thử đổi bộ lọc.</td></tr>
              ) : visible.map((o) => (
                <tr key={o.id}>
                  <td><div className="primary-cell">#{o.orderCode}</div><div className="subcell">{formatDateTime(o.createdAt)}</div></td>
                  <td>{o.recipientName}</td>
                  <td>{o.totalItems}</td>
                  <td className="primary-cell">{formatMoney(o.totalAmount)}</td>
                  <td><OrderStatusBadge status={o.status} /></td>
                  <td>
                    <div className="row" style={{ justifyContent: 'flex-end', gap: 8 }}>
                      <button type="button" className="btn sm" onClick={() => void openDetail(o.id)}>Xem</button>
                      {(NEXT[o.status === 'PACKED' ? 'PACKING' : o.status] ?? []).map((to) => (
                        <button
                          key={to}
                          type="button"
                          className={`btn sm ${to === 'CANCELLED' || to === 'DELIVERY_FAILED' ? 'danger' : 'soft'}`}
                          onClick={() => { setNote(''); setPending({ order: o, to }); }}
                        >
                          {ORDER_STATUS[to]?.label ?? to}
                        </button>
                      ))}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <Pager page={page} totalPages={totalPages} onChange={setPage} />

      <Modal
        open={!!pending}
        title={pending ? `Chuyển đơn #${pending.order.orderCode} sang “${ORDER_STATUS[pending.to]?.label ?? pending.to}”?` : ''}
        onClose={() => setPending(null)}
        actions={<>
          <button type="button" className="btn" onClick={() => setPending(null)}>Hủy</button>
          <button type="button" className="btn primary" disabled={saving} onClick={() => void confirmTransition()}>{saving ? 'Đang lưu...' : 'Xác nhận'}</button>
        </>}
      >
        <p className="muted" style={{ marginBottom: 14 }}>Thao tác được ghi vào lịch sử đơn hàng.</p>
        <label className="field"><span>Ghi chú (tùy chọn)</span>
          <textarea maxLength={500} value={note} onChange={(e) => setNote(e.target.value)} style={{ minHeight: 70 }} />
        </label>
      </Modal>

      <Modal open={!!detail} title={detail ? `Đơn #${detail.orderCode}` : ''} onClose={() => setDetail(null)} wide>
        {detail && (
          <div className="stack">
            <div className="row between wrap">
              <div>
                <strong>{detail.recipientName}</strong> · {detail.recipientPhone}
                <p className="small muted">{detail.shippingAddress}</p>
              </div>
              <OrderStatusBadge status={detail.status} />
            </div>
            <div className="table-wrap">
              <table className="data-table">
                <thead><tr><th>Sản phẩm</th><th>SKU</th><th>SL</th><th>Thành tiền</th></tr></thead>
                <tbody>
                  {detail.items.map((i) => (
                    <tr key={i.id}>
                      <td><div className="primary-cell">{i.productName}</div><div className="subcell">{i.variantName}</div></td>
                      <td>{i.sku}</td><td>{i.quantity}</td><td>{formatMoney(i.lineTotal)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="summary-line total"><span>Tổng cộng</span><strong>{formatMoney(detail.totalAmount)}</strong></div>
            <div>
              <h3 style={{ marginBottom: 8 }}>Lịch sử trạng thái</h3>
              {detail.statusHistories.map((h) => (
                <p key={h.id} className="small"><strong>{ORDER_STATUS[h.toStatus]?.label ?? h.toStatus}</strong> <span className="muted">· {formatDateTime(h.changedAt)}</span></p>
              ))}
            </div>
          </div>
        )}
      </Modal>
    </>
  );
};
