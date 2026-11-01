import { apiErrorMessage, Badge, formatMoney, Icon, Modal, PageHeading, ProductImage, Spinner, useToast } from '@/components/ui';
import { apiClient } from '@/services/api/apiClient';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { catalogApi } from '../../api/catalogApi';
import { Brand, Category, ProductResponse } from '../../types/catalog';

type ProductForm = {
    name: string; description: string; brandId: string; categoryId: string;
    sku: string; ram: string; storage: string; color: string; price: string;
};
const EMPTY: ProductForm = { name: '', description: '', brandId: '', categoryId: '', sku: '', ram: '16GB', storage: '512GB SSD', color: 'Bạc', price: '' };

const adminProductApi = {
    list: async () => (await apiClient.get<ProductResponse[]>('/admin/products')).data,
    create: async (body: object) => (await apiClient.post<{ id: number }>('/admin/products', body)).data,
    update: async (id: number, body: object) => (await apiClient.put<ProductResponse>(`/admin/products/${id}`, body)).data,
    setStatus: async (id: number, status: string) => (await apiClient.patch<string>(`/admin/products/${id}/status`, null, { params: { status } })).data,
    addVariant: async (productId: number, body: object) => (await apiClient.post(`/admin/products/${productId}/variants`, body)).data,
};

const priceRange = (p: ProductResponse) => {
    const prices = (p.variants ?? []).map((v) => v.price).filter((x) => x != null);
    if (!prices.length) return '—';
    const min = Math.min(...prices), max = Math.max(...prices);
    return min === max ? formatMoney(min) : `${formatMoney(min)} – ${formatMoney(max)}`;
};

const AdminProductPage: React.FC = () => {
    const toast = useToast();
    const [products, setProducts] = useState<ProductResponse[]>([]);
    const [brands, setBrands] = useState<Brand[]>([]);
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [keyword, setKeyword] = useState('');
    const [editing, setEditing] = useState<ProductResponse | 'new' | null>(null);
    const [form, setForm] = useState<ProductForm>(EMPTY);
    const [saving, setSaving] = useState(false);
    const [toggling, setToggling] = useState<ProductResponse | null>(null);

    const load = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            setProducts(await adminProductApi.list());
        } catch (err) {
            setError(apiErrorMessage(err, 'Không tải được danh sách sản phẩm.'));
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        void load();
        catalogApi.getBrands().then(setBrands).catch(() => setBrands([]));
        catalogApi.getCategories().then(setCategories).catch(() => setCategories([]));
    }, [load]);

    const visible = useMemo(() => {
        const q = keyword.trim().toLowerCase();
        return products.filter((p) => !q || p.name.toLowerCase().includes(q) || (p.variants ?? []).some((v) => v.sku.toLowerCase().includes(q)));
    }, [products, keyword]);

    const openForm = (p: ProductResponse | 'new') => {
        if (p === 'new') {
            setForm({ ...EMPTY, brandId: brands[0] ? String(brands[0].id) : '', categoryId: categories[0] ? String(categories[0].id) : '' });
        } else {
            setForm({
                ...EMPTY,
                name: p.name,
                description: p.description ?? '',
                brandId: String(brands.find((b) => b.name === p.brandName)?.id ?? ''),
                categoryId: String(categories.find((c) => c.name === p.categoryName)?.id ?? ''),
            });
        }
        setEditing(p);
    };

    const set = (k: keyof ProductForm) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) =>
        setForm((f) => ({ ...f, [k]: k === 'sku' ? e.target.value.toUpperCase() : e.target.value }));

    const submit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!form.name.trim() || !form.brandId || !form.categoryId) {
            toast('Nhập tên, chọn thương hiệu và danh mục.', { tone: 'error' });
            return;
        }
        try {
            setSaving(true);
            if (editing === 'new') {
                const price = Number(form.price);
                if (!/^[A-Z0-9-]+$/.test(form.sku) || !(price >= 1)) {
                    toast('SKU chỉ gồm chữ in hoa, số, dấu “-”; giá phải lớn hơn 0.', { tone: 'error' });
                    return;
                }
                const created = await adminProductApi.create({
                    name: form.name.trim(),
                    description: form.description.trim() || form.name.trim(),
                    sku: form.sku,
                    price,
                    brandId: Number(form.brandId),
                    categoryId: Number(form.categoryId),
                });
                if (created?.id) {
                    await adminProductApi.addVariant(created.id, {
                        productId: created.id,
                        sku: form.sku,
                        variantName: `${form.ram} - ${form.storage}`,
                        price,
                        status: 'ACTIVE',
                        color: form.color.trim() || 'Bạc',
                        ram: form.ram.trim(),
                        storage: form.storage.trim(),
                    });
                }
                toast('Đã thêm sản phẩm. Nhập hàng để có tồn kho bán.');
            } else if (editing) {
                const minPrice = Math.min(...(editing.variants ?? []).map((v) => v.price), Number.MAX_SAFE_INTEGER);
                await adminProductApi.update(editing.id, {
                    id: editing.id,
                    name: form.name.trim(),
                    description: form.description.trim(),
                    price: Number.isFinite(minPrice) && minPrice < Number.MAX_SAFE_INTEGER ? minPrice : 1,
                    isActive: editing.status === 'ACTIVE',
                    brandId: Number(form.brandId),
                    categoryId: Number(form.categoryId),
                });
                toast('Đã cập nhật sản phẩm.');
            }
            setEditing(null);
            await load();
        } catch (err) {
            toast(apiErrorMessage(err, 'Không lưu được sản phẩm.'), { tone: 'error' });
            await load();
        } finally {
            setSaving(false);
        }
    };

    const confirmToggle = async () => {
        if (!toggling) return;
        const next = toggling.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
        try {
            await adminProductApi.setStatus(toggling.id, next);
            toast(next === 'ACTIVE' ? `Đã mở bán ${toggling.name}.` : `Đã ngừng bán ${toggling.name}.`);
            setToggling(null);
            await load();
        } catch (err) {
            toast(apiErrorMessage(err, 'Không đổi được trạng thái.'), { tone: 'error' });
        }
    };

    return (
        <>
            <PageHeading
                eyebrow="Quản trị / Sản phẩm"
                title="Danh mục sản phẩm"
                sub="Quản lý thông tin, cấu hình và trạng thái bán."
                actions={<button type="button" className="btn sm primary" onClick={() => openForm('new')}><Icon name="plus" /> Thêm sản phẩm</button>}
            />
            <div className="ops-toolbar">
                <label className="search">
                    <Icon name="search" />
                    <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Tìm tên sản phẩm hoặc SKU..." aria-label="Tìm sản phẩm" />
                </label>
                <span className="small muted push">{visible.length} sản phẩm</span>
            </div>

            {loading ? <Spinner /> : (
                <div className="table-wrap">
                    <table className="data-table">
                        <thead><tr><th>Sản phẩm</th><th>Thương hiệu / Danh mục</th><th>Cấu hình</th><th>Giá bán</th><th>Tồn kho</th><th>Trạng thái</th><th style={{ textAlign: 'right' }}>Thao tác</th></tr></thead>
                        <tbody>
                            {error ? <tr><td colSpan={7} className="table-empty">{error}</td></tr>
                                : visible.length === 0 ? <tr><td colSpan={7} className="table-empty">Chưa có sản phẩm phù hợp.</td></tr>
                                : visible.map((p) => {
                                    const img = (p.images ?? []).find((i) => i.isPrimary)?.imageUrl ?? p.images?.[0]?.imageUrl;
                                    const stock = (p.variants ?? []).reduce((s, v) => s + (v.quantity || 0), 0);
                                    return (
                                        <tr key={p.id}>
                                            <td>
                                                <div className="row" style={{ gap: 12 }}>
                                                    <ProductImage className="thumb-cell" src={img} alt={p.name} hint={`${p.categoryName} ${p.name}`} />
                                                    <div><Link to={`/products/${p.id}`} className="primary-cell">{p.name}</Link><div className="subcell">ID {p.id}</div></div>
                                                </div>
                                            </td>
                                            <td><div>{p.brandName}</div><div className="subcell">{p.categoryName}</div></td>
                                            <td>{(p.variants ?? []).length} biến thể</td>
                                            <td>{priceRange(p)}</td>
                                            <td className="primary-cell">{stock}</td>
                                            <td>{p.status === 'ACTIVE' ? <Badge tone="green">Đang bán</Badge> : <Badge tone="red">Ngừng bán</Badge>}</td>
                                            <td>
                                                <div className="row" style={{ justifyContent: 'flex-end', gap: 8 }}>
                                                    <button type="button" className="btn sm" onClick={() => openForm(p)}><Icon name="edit" /> Sửa</button>
                                                    <button type="button" className={`btn sm ${p.status === 'ACTIVE' ? 'danger' : 'soft'}`} onClick={() => setToggling(p)}>
                                                        {p.status === 'ACTIVE' ? 'Ngừng bán' : 'Mở bán'}
                                                    </button>
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
                open={!!editing}
                wide
                title={editing === 'new' ? 'Thêm sản phẩm' : 'Sửa sản phẩm'}
                onClose={() => setEditing(null)}
                actions={<>
                    <button type="button" className="btn" onClick={() => setEditing(null)}>Hủy</button>
                    <button type="submit" form="product-form" className="btn primary" disabled={saving}>{saving ? 'Đang lưu...' : 'Lưu sản phẩm'}</button>
                </>}
            >
                <form id="product-form" className="stack" onSubmit={submit}>
                    <label className="field"><span>Tên sản phẩm *</span><input required value={form.name} onChange={set('name')} /></label>
                    <div className="field-row">
                        <label className="field"><span>Thương hiệu *</span>
                            <select required value={form.brandId} onChange={set('brandId')}>
                                <option value="">— Chọn —</option>
                                {brands.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
                            </select>
                        </label>
                        <label className="field"><span>Danh mục *</span>
                            <select required value={form.categoryId} onChange={set('categoryId')}>
                                <option value="">— Chọn —</option>
                                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                            </select>
                        </label>
                    </div>
                    <label className="field"><span>Mô tả</span><textarea value={form.description} onChange={set('description')} style={{ minHeight: 80 }} /></label>
                    {editing === 'new' && (
                        <>
                            <h3 style={{ marginTop: 6 }}>Cấu hình đầu tiên</h3>
                            <div className="field-row">
                                <label className="field"><span>SKU *</span><input required value={form.sku} onChange={set('sku')} placeholder="ASU-ZEN14-16-512" /></label>
                                <label className="field"><span>Giá bán (₫) *</span><input type="number" min={1} step={1000} required value={form.price} onChange={set('price')} /></label>
                            </div>
                            <div className="field-row" style={{ gridTemplateColumns: '1fr 1fr 1fr' }}>
                                <label className="field"><span>RAM</span><input required value={form.ram} onChange={set('ram')} /></label>
                                <label className="field"><span>Ổ cứng</span><input required value={form.storage} onChange={set('storage')} /></label>
                                <label className="field"><span>Màu</span><input required value={form.color} onChange={set('color')} /></label>
                            </div>
                            <p className="small muted">Tồn kho ban đầu là 0; tăng tồn bằng phiếu nhập ở mục Nhập hàng.</p>
                        </>
                    )}
                </form>
            </Modal>

            <Modal
                open={!!toggling}
                title={toggling?.status === 'ACTIVE' ? 'Ngừng bán sản phẩm?' : 'Mở bán sản phẩm?'}
                onClose={() => setToggling(null)}
                actions={<>
                    <button type="button" className="btn" onClick={() => setToggling(null)}>Hủy</button>
                    <button type="button" className="btn primary" onClick={() => void confirmToggle()}>Xác nhận</button>
                </>}
            >
                <p className="muted">{toggling?.name} sẽ {toggling?.status === 'ACTIVE' ? 'không thể thêm vào giỏ hàng' : 'được bán trở lại'}.</p>
            </Modal>
        </>
    );
};

export default AdminProductPage;
