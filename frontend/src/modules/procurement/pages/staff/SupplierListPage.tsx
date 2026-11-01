import { useAuth } from '@/app/providers/AuthProvider';
import { apiErrorMessage, Badge, Icon, Modal, PageHeading, Spinner, useToast } from '@/components/ui';
import { ROLES } from '@/constants/roles';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { supplierApi } from '../../api/supplierApi';
import { Supplier } from '../../types';

type SupplierForm = { name: string; phone: string; email: string; address: string };
const EMPTY: SupplierForm = { name: '', phone: '', email: '', address: '' };

const SupplierListPage: React.FC = () => {
    const { user } = useAuth();
    const toast = useToast();
    const isAdmin = !!user?.roles.includes(ROLES.ADMIN);
    const [suppliers, setSuppliers] = useState<Supplier[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [keyword, setKeyword] = useState('');
    const [editing, setEditing] = useState<Supplier | 'new' | null>(null);
    const [form, setForm] = useState<SupplierForm>(EMPTY);
    const [saving, setSaving] = useState(false);

    const load = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            setSuppliers(await supplierApi.getAllSuppliers());
        } catch {
            setError('Không tải được danh sách nhà cung cấp.');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { void load(); }, [load]);

    const visible = useMemo(() => {
        const q = keyword.trim().toLowerCase();
        return suppliers.filter((s) => !q || [s.name, s.email, s.phone].some((v) => v?.toLowerCase().includes(q)));
    }, [suppliers, keyword]);

    const openForm = (s: Supplier | 'new') => {
        setForm(s === 'new' ? EMPTY : { name: s.name ?? '', phone: s.phone ?? '', email: s.email ?? '', address: s.address ?? '' });
        setEditing(s);
    };

    const submit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!form.name.trim()) {
            toast('Tên nhà cung cấp không được để trống.', { tone: 'error' });
            return;
        }
        const payload = { name: form.name.trim(), phone: form.phone.trim(), email: form.email.trim(), address: form.address.trim() };
        try {
            setSaving(true);
            if (editing === 'new') {
                await supplierApi.createSupplier(payload);
                toast('Đã thêm nhà cung cấp.');
            } else if (editing) {
                await supplierApi.updateSupplier(editing.id, { ...payload, status: editing.status || 'ACTIVE' });
                toast('Đã cập nhật nhà cung cấp.');
            }
            setEditing(null);
            await load();
        } catch (err) {
            toast(apiErrorMessage(err, 'Không lưu được nhà cung cấp.'), { tone: 'error' });
        } finally {
            setSaving(false);
        }
    };

    const toggleStatus = async (s: Supplier) => {
        const next = s.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
        try {
            await supplierApi.toggleSupplierStatus(s.id, next);
            toast(next === 'ACTIVE' ? `Đã mở lại hợp tác với ${s.name}.` : `Đã tạm ngưng ${s.name}.`);
            await load();
        } catch (err) {
            toast(apiErrorMessage(err, 'Không đổi được trạng thái.'), { tone: 'error' });
        }
    };

    const set = (k: keyof SupplierForm) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => setForm((f) => ({ ...f, [k]: e.target.value }));

    return (
        <>
            <PageHeading
                eyebrow="Vận hành / Nhà cung cấp"
                title="Đối tác cung ứng"
                sub="Thông tin liên hệ để lập phiếu nhập hàng."
                actions={isAdmin
                    ? <button type="button" className="btn sm primary" onClick={() => openForm('new')}><Icon name="plus" /> Thêm nhà cung cấp</button>
                    : <span className="small muted">Chỉ Admin được thêm/sửa nhà cung cấp</span>}
            />
            <div className="ops-toolbar">
                <label className="search">
                    <Icon name="search" />
                    <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Tìm tên, email, số điện thoại..." aria-label="Tìm nhà cung cấp" />
                </label>
                <span className="small muted push">{visible.length} nhà cung cấp</span>
            </div>
            {loading ? <Spinner /> : (
                <div className="table-wrap">
                    <table className="data-table">
                        <thead><tr><th>Nhà cung cấp</th><th>Liên hệ</th><th>Địa chỉ</th><th>Trạng thái</th>{isAdmin && <th style={{ textAlign: 'right' }}>Thao tác</th>}</tr></thead>
                        <tbody>
                            {error ? <tr><td colSpan={5} className="table-empty">{error}</td></tr>
                                : visible.length === 0 ? <tr><td colSpan={5} className="table-empty">Chưa có nhà cung cấp nào.</td></tr>
                                : visible.map((s) => (
                                    <tr key={s.id}>
                                        <td className="primary-cell">{s.name}</td>
                                        <td><div>{s.email || '—'}</div><div className="subcell">{s.phone || '—'}</div></td>
                                        <td style={{ whiteSpace: 'normal', maxWidth: 280 }}>{s.address || '—'}</td>
                                        <td>{s.status === 'ACTIVE' ? <Badge tone="green">Đang hợp tác</Badge> : <Badge>Tạm ngưng</Badge>}</td>
                                        {isAdmin && (
                                            <td>
                                                <div className="row" style={{ justifyContent: 'flex-end', gap: 8 }}>
                                                    <button type="button" className="btn sm" onClick={() => openForm(s)}><Icon name="edit" /> Sửa</button>
                                                    <button type="button" className={`btn sm ${s.status === 'ACTIVE' ? 'danger' : 'soft'}`} onClick={() => void toggleStatus(s)}>
                                                        {s.status === 'ACTIVE' ? 'Tạm ngưng' : 'Kích hoạt'}
                                                    </button>
                                                </div>
                                            </td>
                                        )}
                                    </tr>
                                ))}
                        </tbody>
                    </table>
                </div>
            )}

            <Modal
                open={!!editing}
                title={editing === 'new' ? 'Thêm nhà cung cấp' : 'Sửa nhà cung cấp'}
                onClose={() => setEditing(null)}
                actions={<>
                    <button type="button" className="btn" onClick={() => setEditing(null)}>Hủy</button>
                    <button type="submit" form="supplier-form" className="btn primary" disabled={saving}>{saving ? 'Đang lưu...' : 'Lưu'}</button>
                </>}
            >
                <form id="supplier-form" className="stack" onSubmit={submit}>
                    <label className="field"><span>Tên nhà cung cấp *</span><input required value={form.name} onChange={set('name')} /></label>
                    <div className="field-row">
                        <label className="field"><span>Email</span><input type="email" value={form.email} onChange={set('email')} /></label>
                        <label className="field"><span>Số điện thoại</span><input type="tel" value={form.phone} onChange={set('phone')} /></label>
                    </div>
                    <label className="field"><span>Địa chỉ</span><textarea value={form.address} onChange={set('address')} style={{ minHeight: 70 }} /></label>
                </form>
            </Modal>
        </>
    );
};

export default SupplierListPage;
