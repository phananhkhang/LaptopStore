import { apiErrorMessage, Badge, BadgeTone, formatDateTime, formatMoney, Icon, Modal, PageHeading, Spinner, useToast } from '@/components/ui';
import { catalogApi } from '@/modules/catalog/api/catalogApi';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { purchaseOrderApi } from '../../api/purchaseOrderApi';
import { supplierApi } from '../../api/supplierApi';
import { PurchaseOrder, Supplier } from '../../types';

const STATUS: Record<string, { label: string; tone: BadgeTone }> = {
    DRAFT: { label: 'Nháp', tone: 'neutral' },
    PARTIALLY_RECEIVED: { label: 'Nhận một phần', tone: 'orange' },
    RECEIVED: { label: 'Đã nhận đủ', tone: 'green' },
    CANCELLED: { label: 'Đã hủy', tone: 'red' },
};

interface VariantOption { id: number; label: string; price: number }
interface Line { key: number; variantId: string; quantity: string; unitCost: string }

const newLine = (): Line => ({ key: Date.now() + Math.random(), variantId: '', quantity: '1', unitCost: '' });
const idempotencyKey = () => `po-receive-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;

const PurchaseOrderListPage: React.FC = () => {
    const toast = useToast();
    const [searchParams, setSearchParams] = useSearchParams();
    const [orders, setOrders] = useState<PurchaseOrder[]>([]);
    const [suppliers, setSuppliers] = useState<Supplier[]>([]);
    const [variants, setVariants] = useState<VariantOption[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [creating, setCreating] = useState(false);
    const [supplierId, setSupplierId] = useState('');
    const [note, setNote] = useState('');
    const [lines, setLines] = useState<Line[]>([newLine()]);
    const [saving, setSaving] = useState(false);

    const [receiving, setReceiving] = useState<{ po: PurchaseOrder; qty: Record<number, string>; key: string } | null>(null);
    const [cancelling, setCancelling] = useState<PurchaseOrder | null>(null);

    const variantLabel = useMemo(() => Object.fromEntries(variants.map((v) => [v.id, v.label])), [variants]);

    const load = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            setOrders(await purchaseOrderApi.getAllPurchaseOrders());
        } catch {
            setError('Không tải được danh sách phiếu nhập.');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        void load();
        supplierApi.getAllSuppliers().then(setSuppliers).catch(() => setSuppliers([]));
        catalogApi.getAllProducts()
            .then((products) => setVariants(products.flatMap((p) => (p.variants ?? []).map((v) => ({
                id: v.id,
                label: `${p.name} · ${v.ram}/${v.storage} · ${v.sku}`,
                price: v.price,
            })))))
            .catch(() => setVariants([]));
    }, [load]);

    useEffect(() => {
        if (searchParams.get('new') === '1') {
            setCreating(true);
            const next = new URLSearchParams(searchParams);
            next.delete('new');
            setSearchParams(next, { replace: true });
        }
    }, [searchParams, setSearchParams]);

    const openCreate = () => {
        setSupplierId('');
        setNote('');
        setLines([newLine()]);
        setCreating(true);
    };

    const updateLine = (key: number, patch: Partial<Line>) => setLines((ls) => ls.map((l) => (l.key === key ? { ...l, ...patch } : l)));

    const submitCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        const items = lines.map((l) => ({ variantId: Number(l.variantId), quantity: Number(l.quantity), unitCost: Number(l.unitCost) }));
        if (!supplierId) return toast('Chọn nhà cung cấp.', { tone: 'error' });
        if (items.some((i) => !i.variantId || !Number.isInteger(i.quantity) || i.quantity < 1 || !(i.unitCost >= 0) || Number.isNaN(i.unitCost))) {
            return toast('Kiểm tra lại SKU, số lượng (≥ 1) và đơn giá nhập.', { tone: 'error' });
        }
        if (new Set(items.map((i) => i.variantId)).size !== items.length) {
            return toast('Mỗi SKU chỉ xuất hiện một dòng.', { tone: 'error' });
        }
        try {
            setSaving(true);
            await purchaseOrderApi.createPurchaseOrder({ supplierId: Number(supplierId), note: note.trim() || undefined, items });
            toast('Đã tạo phiếu nhập nháp.');
            setCreating(false);
            await load();
        } catch (err) {
            toast(apiErrorMessage(err, 'Không tạo được phiếu nhập.'), { tone: 'error' });
        } finally {
            setSaving(false);
        }
    };

    const openReceive = (po: PurchaseOrder) => {
        const qty: Record<number, string> = {};
        po.items.forEach((i) => { qty[i.variantId] = String(Math.max(0, i.quantity - (i.receivedQuantity ?? 0))); });
        setReceiving({ po, qty, key: idempotencyKey() });
    };

    const submitReceive = async () => {
        if (!receiving) return;
        const receiptLines = receiving.po.items
            .map((i) => ({ variantId: i.variantId, quantity: Number(receiving.qty[i.variantId] || 0), remaining: i.quantity - (i.receivedQuantity ?? 0) }))
            .filter((l) => l.quantity > 0);
        if (receiptLines.length === 0) return toast('Nhập số thực nhận cho ít nhất một sản phẩm.', { tone: 'error' });
        if (receiptLines.some((l) => !Number.isInteger(l.quantity) || l.quantity > l.remaining)) {
            return toast('Số nhận phải là số nguyên và không vượt số còn thiếu.', { tone: 'error' });
        }
        try {
            setSaving(true);
            await purchaseOrderApi.receivePurchaseOrder(receiving.po.id, {
                lines: receiptLines.map(({ variantId, quantity }) => ({ variantId, quantity })),
                idempotencyKey: receiving.key,
            });
            toast('Đã nhận hàng và cộng tồn kho.');
            setReceiving(null);
            await load();
        } catch (err) {
            toast(apiErrorMessage(err, 'Không ghi nhận được phiếu nhập.'), { tone: 'error' });
        } finally {
            setSaving(false);
        }
    };

    const submitCancel = async () => {
        if (!cancelling) return;
        try {
            await purchaseOrderApi.cancelPurchaseOrder(cancelling.id);
            toast(`Đã hủy phiếu PN-${cancelling.id}.`);
            setCancelling(null);
            await load();
        } catch (err) {
            toast(apiErrorMessage(err, 'Không hủy được phiếu nhập.'), { tone: 'error' });
        }
    };

    const total = (po: PurchaseOrder) => po.items.reduce((s, i) => s + i.quantity * Number(i.unitCost), 0);

    return (
        <>
            <PageHeading
                eyebrow="Vận hành / Nhập hàng"
                title="Phiếu nhập hàng"
                sub="Lập phiếu, nhận một phần hoặc nhận đủ; tồn kho được cộng tự động."
                actions={<button type="button" className="btn sm primary" onClick={openCreate}><Icon name="plus" /> Lập phiếu nhập</button>}
            />

            {loading ? <Spinner /> : (
                <div className="table-wrap">
                    <table className="data-table">
                        <thead><tr><th>Mã phiếu</th><th>Nhà cung cấp</th><th>Mặt hàng</th><th>Giá trị</th><th>Trạng thái</th><th style={{ textAlign: 'right' }}>Thao tác</th></tr></thead>
                        <tbody>
                            {error ? <tr><td colSpan={6} className="table-empty">{error}</td></tr>
                                : orders.length === 0 ? <tr><td colSpan={6} className="table-empty">Chưa có phiếu nhập. Bấm “Lập phiếu nhập” để bắt đầu.</td></tr>
                                : orders.map((po) => {
                                    const s = STATUS[po.status] ?? { label: po.status, tone: 'neutral' as BadgeTone };
                                    const open = po.status === 'DRAFT' || po.status === 'PARTIALLY_RECEIVED';
                                    return (
                                        <tr key={po.id}>
                                            <td><div className="primary-cell">PN-{po.id}</div><div className="subcell">{formatDateTime(po.createdAt)}</div></td>
                                            <td>{po.supplier?.name ?? '—'}</td>
                                            <td>
                                                <div>{po.items.length} SKU · {po.items.reduce((n, i) => n + i.quantity, 0)} máy</div>
                                                <div className="subcell">Đã nhận {po.items.reduce((n, i) => n + (i.receivedQuantity ?? 0), 0)}</div>
                                            </td>
                                            <td className="primary-cell">{formatMoney(total(po))}</td>
                                            <td><Badge tone={s.tone}>{s.label}</Badge></td>
                                            <td>
                                                <div className="row" style={{ justifyContent: 'flex-end', gap: 8 }}>
                                                    {open && <button type="button" className="btn sm soft" onClick={() => openReceive(po)}>Nhận hàng</button>}
                                                    {po.status === 'DRAFT' && <button type="button" className="btn sm danger" onClick={() => setCancelling(po)}>Hủy</button>}
                                                    {!open && <span className="small muted">—</span>}
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })}
                        </tbody>
                    </table>
                </div>
            )}

            <Modal
                open={creating}
                title="Lập phiếu nhập"
                wide
                onClose={() => setCreating(false)}
                actions={<>
                    <button type="button" className="btn" onClick={() => setCreating(false)}>Hủy</button>
                    <button type="submit" form="po-form" className="btn primary" disabled={saving}>{saving ? 'Đang lưu...' : 'Tạo phiếu nháp'}</button>
                </>}
            >
                <form id="po-form" className="stack" onSubmit={submitCreate}>
                    <label className="field"><span>Nhà cung cấp *</span>
                        <select required value={supplierId} onChange={(e) => setSupplierId(e.target.value)}>
                            <option value="">— Chọn nhà cung cấp —</option>
                            {suppliers.filter((s) => s.status !== 'INACTIVE').map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                        </select>
                        {suppliers.length === 0 && <small>Chưa có nhà cung cấp. Admin cần thêm ở mục Nhà cung cấp.</small>}
                    </label>
                    {lines.map((l, idx) => (
                        <div key={l.key} className="field-row" style={{ gridTemplateColumns: '2.2fr .7fr 1fr auto', alignItems: 'end', gap: 10 }}>
                            <label className="field"><span>{idx === 0 ? 'Sản phẩm / SKU *' : ' '}</span>
                                <select required value={l.variantId} onChange={(e) => {
                                    const v = variants.find((x) => x.id === Number(e.target.value));
                                    updateLine(l.key, { variantId: e.target.value, unitCost: l.unitCost || (v ? String(Math.round(v.price * 0.8)) : '') });
                                }}>
                                    <option value="">— Chọn SKU —</option>
                                    {variants.map((v) => <option key={v.id} value={v.id}>{v.label}</option>)}
                                </select>
                            </label>
                            <label className="field"><span>{idx === 0 ? 'Số lượng' : ' '}</span>
                                <input type="number" min={1} step={1} required value={l.quantity} onChange={(e) => updateLine(l.key, { quantity: e.target.value })} />
                            </label>
                            <label className="field"><span>{idx === 0 ? 'Đơn giá nhập (₫)' : ' '}</span>
                                <input type="number" min={0} step={1000} required value={l.unitCost} onChange={(e) => updateLine(l.key, { unitCost: e.target.value })} />
                            </label>
                            <button type="button" className="icon-btn" aria-label="Xóa dòng" disabled={lines.length < 2} onClick={() => setLines((ls) => ls.filter((x) => x.key !== l.key))}>
                                <Icon name="trash" />
                            </button>
                        </div>
                    ))}
                    <button type="button" className="text-btn small" onClick={() => setLines((ls) => [...ls, newLine()])}><Icon name="plus" /> Thêm dòng</button>
                    <label className="field"><span>Ghi chú</span><textarea value={note} onChange={(e) => setNote(e.target.value)} style={{ minHeight: 60 }} /></label>
                </form>
            </Modal>

            <Modal
                open={!!receiving}
                title={`Nhận hàng · PN-${receiving?.po.id ?? ''}`}
                wide
                onClose={() => setReceiving(null)}
                actions={<>
                    <button type="button" className="btn" onClick={() => setReceiving(null)}>Hủy</button>
                    <button type="button" className="btn primary" disabled={saving} onClick={() => void submitReceive()}>{saving ? 'Đang lưu...' : 'Xác nhận nhận hàng'}</button>
                </>}
            >
                {receiving && (
                    <div className="table-wrap">
                        <table className="data-table">
                            <thead><tr><th>SKU</th><th>Đặt</th><th>Đã nhận</th><th>Nhận lần này</th></tr></thead>
                            <tbody>
                                {receiving.po.items.map((i) => (
                                    <tr key={i.variantId}>
                                        <td style={{ whiteSpace: 'normal' }}>{variantLabel[i.variantId] ?? `Biến thể #${i.variantId}`}</td>
                                        <td>{i.quantity}</td>
                                        <td>{i.receivedQuantity ?? 0}</td>
                                        <td>
                                            <input
                                                className="input"
                                                style={{ width: 90 }}
                                                type="number"
                                                min={0}
                                                max={i.quantity - (i.receivedQuantity ?? 0)}
                                                value={receiving.qty[i.variantId] ?? '0'}
                                                onChange={(e) => setReceiving((r) => r && { ...r, qty: { ...r.qty, [i.variantId]: e.target.value } })}
                                                aria-label="Số lượng nhận"
                                            />
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </Modal>

            <Modal
                open={!!cancelling}
                title={`Hủy phiếu PN-${cancelling?.id ?? ''}?`}
                onClose={() => setCancelling(null)}
                actions={<>
                    <button type="button" className="btn" onClick={() => setCancelling(null)}>Không</button>
                    <button type="button" className="btn primary" onClick={() => void submitCancel()}>Hủy phiếu</button>
                </>}
            >
                <p className="muted">Phiếu đã hủy không thể nhận hàng.</p>
            </Modal>
        </>
    );
};

export default PurchaseOrderListPage;
