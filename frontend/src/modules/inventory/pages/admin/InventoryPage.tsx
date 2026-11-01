import { useAuth } from '@/app/providers/AuthProvider';
import { apiErrorMessage, Badge, formatDateTime, formatMoney, Icon, Modal, PageHeading, ProductImage, Spinner, useToast } from '@/components/ui';
import { ROLES } from '@/constants/roles';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { catalogApi } from '../../../catalog/api/catalogApi';
import { VariantSnapshot } from '../../../catalog/types/catalog';
import { inventoryApi } from '../../api/inventoryApi';
import { StockMovement } from '../../types';

interface InventoryItem extends VariantSnapshot {
  productId: number;
  productName: string;
  categoryName: string;
  thumbnail: string | null;
}

const LOW_STOCK = 5;

export const InventoryPage: React.FC = () => {
  const { user } = useAuth();
  const toast = useToast();
  const isAdmin = !!user?.roles.includes(ROLES.ADMIN);
  const [searchParams, setSearchParams] = useSearchParams();
  const [items, setItems] = useState<InventoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [keyword, setKeyword] = useState('');
  const [category, setCategory] = useState('');
  const lowOnly = searchParams.get('low') === '1';

  const [adjusting, setAdjusting] = useState<InventoryItem | null>(null);
  const [delta, setDelta] = useState('');
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [history, setHistory] = useState<{ item: InventoryItem; rows: StockMovement[] | null } | null>(null);

  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const products = await catalogApi.getAllProducts();
      setItems(products.flatMap((p) => (p.variants ?? []).map((v) => ({
        ...v,
        productId: p.id,
        productName: p.name,
        categoryName: p.categoryName,
        thumbnail: (p.images ?? []).find((i) => i.isPrimary)?.imageUrl ?? p.images?.[0]?.imageUrl ?? null,
      }))));
    } catch {
      setError('Không tải được dữ liệu kho. Kiểm tra kết nối backend.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const categories = useMemo(() => [...new Set(items.map((i) => i.categoryName).filter(Boolean))].sort(), [items]);

  const visible = useMemo(() => {
    const q = keyword.trim().toLowerCase();
    return items.filter((i) => (!q || i.sku.toLowerCase().includes(q) || i.productName.toLowerCase().includes(q))
      && (!category || i.categoryName === category)
      && (!lowOnly || i.quantity <= LOW_STOCK));
  }, [items, keyword, category, lowOnly]);

  const metrics = useMemo(() => ({
    total: items.length,
    stock: items.reduce((s, i) => s + (i.quantity || 0), 0),
    low: items.filter((i) => i.quantity > 0 && i.quantity <= LOW_STOCK).length,
    out: items.filter((i) => i.quantity === 0).length,
  }), [items]);

  const toggleLow = () => {
    const next = new URLSearchParams(searchParams);
    if (lowOnly) next.delete('low'); else next.set('low', '1');
    setSearchParams(next);
  };

  const openHistory = async (item: InventoryItem) => {
    setHistory({ item, rows: null });
    try {
      const res = await inventoryApi.getVariantHistory(item.id, 0, 20);
      setHistory({ item, rows: res.data.content });
    } catch (err) {
      toast(apiErrorMessage(err, 'Không tải được lịch sử kho.'), { tone: 'error' });
      setHistory({ item, rows: [] });
    }
  };

  const submitAdjust = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!adjusting) return;
    const d = Number(delta);
    if (!Number.isInteger(d) || d === 0) {
      toast('Số lượng thay đổi phải là số nguyên khác 0.', { tone: 'error' });
      return;
    }
    if (adjusting.quantity + d < 0) {
      toast('Điều chỉnh này làm tồn kho âm.', { tone: 'error' });
      return;
    }
    if (reason.trim().length < 5) {
      toast('Lý do cần tối thiểu 5 ký tự.', { tone: 'error' });
      return;
    }
    try {
      setSaving(true);
      await inventoryApi.adjustStock(adjusting.id, { delta: d, reason: reason.trim() });
      toast(`Đã điều chỉnh ${adjusting.sku}: ${adjusting.quantity} → ${adjusting.quantity + d}.`);
      setAdjusting(null);
      await load();
    } catch (err) {
      toast(apiErrorMessage(err, 'Không thể điều chỉnh tồn kho.'), { tone: 'error' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <PageHeading
        eyebrow="Vận hành / Kho hàng"
        title="Tồn kho luôn trong tầm mắt."
        sub="Theo dõi tồn thực tế theo từng SKU."
        actions={<>
          <button type="button" className="btn sm" onClick={() => void load()}><Icon name="refresh" /> Làm mới</button>
          <Link to="/staff/procurement?new=1" className="btn sm primary"><Icon name="plus" /> Tạo phiếu nhập</Link>
        </>}
      />

      <div className="metrics">
        <div className="metric"><div className="metric-label">Tổng biến thể (SKU)<Icon name="box" /></div><div className="metric-value">{metrics.total}</div><small>Đang quản lý</small></div>
        <div className="metric"><div className="metric-label">Số lượng tồn<Icon name="chart" /></div><div className="metric-value">{metrics.stock.toLocaleString('vi-VN')}</div><small>Máy sẵn sàng giao</small></div>
        <div className="metric"><div className="metric-label">Sắp hết hàng<Icon name="alert" /></div><div className="metric-value">{metrics.low}</div><small className="warn">Tồn ≤ {LOW_STOCK} máy</small></div>
        <div className="metric"><div className="metric-label">Hết hàng<Icon name="close" /></div><div className="metric-value">{metrics.out}</div><small className="bad">Cần nhập gấp</small></div>
      </div>

      <div className="ops-toolbar">
        <label className="search">
          <Icon name="search" />
          <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Tìm tên laptop, mã SKU..." aria-label="Tìm trong kho" />
        </label>
        <select className="select" value={category} onChange={(e) => setCategory(e.target.value)} aria-label="Lọc danh mục">
          <option value="">Tất cả danh mục</option>
          {categories.map((c) => <option key={c} value={c}>{c}</option>)}
        </select>
        <button type="button" className={`chip ${lowOnly ? 'active' : ''}`} onClick={toggleLow} aria-pressed={lowOnly}>Tồn kho thấp</button>
        <span className="small muted push">{visible.length} SKU</span>
      </div>

      {loading ? <Spinner /> : (
        <div className="table-wrap">
          <table className="data-table">
            <thead><tr><th>Sản phẩm / Cấu hình</th><th>SKU</th><th>Giá bán</th><th>Tồn kho</th><th>Trạng thái</th><th style={{ textAlign: 'right' }}>Thao tác</th></tr></thead>
            <tbody>
              {error ? <tr><td colSpan={6} className="table-empty">{error}</td></tr>
                : visible.length === 0 ? <tr><td colSpan={6} className="table-empty">Không có kết quả phù hợp. Thử bỏ bớt điều kiện lọc.</td></tr>
                : visible.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <div className="row" style={{ gap: 12 }}>
                        <ProductImage className="thumb-cell" src={item.thumbnail} alt={item.productName} hint={`${item.categoryName} ${item.productName}`} />
                        <div><div className="primary-cell">{item.productName}</div><div className="subcell">{item.ram} · {item.storage} · {item.color}</div></div>
                      </div>
                    </td>
                    <td>{item.sku}</td>
                    <td>{formatMoney(item.price)}</td>
                    <td className="primary-cell">{item.quantity}</td>
                    <td>
                      {item.quantity === 0 ? <Badge tone="red">Hết hàng</Badge>
                        : item.quantity <= LOW_STOCK ? <Badge tone="orange">Sắp hết hàng</Badge>
                        : <Badge tone="green">Còn hàng</Badge>}
                    </td>
                    <td>
                      <div className="row" style={{ justifyContent: 'flex-end', gap: 8 }}>
                        <button type="button" className="btn sm" onClick={() => void openHistory(item)}>Lịch sử</button>
                        {isAdmin && (
                          <button type="button" className="btn sm soft" onClick={() => { setDelta(''); setReason(''); setAdjusting(item); }}>
                            Điều chỉnh
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      )}
      {!isAdmin && <p className="small muted" style={{ marginTop: 12 }}>Điều chỉnh tồn kho thủ công chỉ dành cho Admin. Nhân viên nhập hàng qua phiếu nhập.</p>}

      <Modal
        open={!!adjusting}
        title={`Điều chỉnh tồn · ${adjusting?.sku ?? ''}`}
        onClose={() => setAdjusting(null)}
        actions={<>
          <button type="button" className="btn" onClick={() => setAdjusting(null)}>Hủy</button>
          <button type="submit" form="adjust-form" className="btn primary" disabled={saving}>{saving ? 'Đang lưu...' : 'Xác nhận điều chỉnh'}</button>
        </>}
      >
        <form id="adjust-form" className="stack" onSubmit={submitAdjust}>
          <p className="muted small">{adjusting?.productName} · Tồn hiện tại <strong>{adjusting?.quantity}</strong></p>
          <label className="field"><span>Số lượng thay đổi (âm để giảm)</span>
            <input type="number" step={1} required value={delta} onChange={(e) => setDelta(e.target.value)} placeholder="Ví dụ: 5 hoặc -2" />
          </label>
          <label className="field"><span>Lý do *</span>
            <textarea required minLength={5} value={reason} onChange={(e) => setReason(e.target.value)} placeholder="Kiểm kê cuối tháng, hàng lỗi..." style={{ minHeight: 70 }} />
          </label>
        </form>
      </Modal>

      <Modal open={!!history} title={`Lịch sử kho · ${history?.item.sku ?? ''}`} onClose={() => setHistory(null)} wide>
        {history?.rows === null ? <Spinner /> : (
          <div className="table-wrap">
            <table className="data-table">
              <thead><tr><th>Thời gian</th><th>Loại</th><th>Thay đổi</th><th>Tồn</th><th>Ghi chú</th></tr></thead>
              <tbody>
                {(history?.rows ?? []).length === 0 ? <tr><td colSpan={5} className="table-empty">Chưa có biến động kho.</td></tr>
                  : history!.rows!.map((m) => (
                    <tr key={m.id}>
                      <td>{formatDateTime(m.createdAt)}</td>
                      <td>{m.type}</td>
                      <td style={{ color: m.delta > 0 ? 'var(--green)' : 'var(--red)', fontWeight: 600 }}>{m.delta > 0 ? `+${m.delta}` : m.delta}</td>
                      <td>{m.quantityBefore} → {m.quantityAfter}</td>
                      <td style={{ whiteSpace: 'normal' }}>{m.note || m.referenceType || '—'}</td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        )}
      </Modal>
    </>
  );
};

export default InventoryPage;
