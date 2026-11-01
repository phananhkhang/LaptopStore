import { apiErrorMessage, Badge, Icon, Modal, PageHeading, Spinner, useToast } from '@/components/ui';
import React, { useEffect, useState } from 'react';
import { categoryApi } from '../../api/categoryApi';
import { Category, CreateCategoryRequest } from '../../types';

export const AdminCategoryPage: React.FC = () => {
  const toast = useToast();
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [name, setName] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [deleting, setDeleting] = useState<Category | null>(null);

  const fetchCategories = async () => {
    setLoading(true);
    setError(null);
    try {
      setCategories(await categoryApi.getAllCategories());
    } catch (err: unknown) {
      setError(apiErrorMessage(err, 'Không thể tải danh sách danh mục.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void fetchCategories();
  }, []);

  const openModal = (cat: Category | null) => {
    setEditingCategory(cat);
    setName(cat?.name ?? '');
    setDescription(cat?.description ?? '');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      toast('Tên danh mục không được để trống.', { tone: 'error' });
      return;
    }
    setSubmitting(true);
    try {
      const payload: CreateCategoryRequest = { name: name.trim(), description: description.trim() || undefined };
      if (editingCategory) {
        await categoryApi.updateCategory(editingCategory.id, payload);
        toast('Đã cập nhật danh mục.');
      } else {
        await categoryApi.createCategory(payload);
        toast('Đã thêm danh mục mới.');
      }
      setIsModalOpen(false);
      await fetchCategories();
    } catch (err: unknown) {
      toast(apiErrorMessage(err, 'Lưu danh mục thất bại.'), { tone: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  const confirmDelete = async () => {
    if (!deleting) return;
    try {
      await categoryApi.deleteCategory(deleting.id);
      toast(`Đã xóa danh mục “${deleting.name}”.`);
      setDeleting(null);
      await fetchCategories();
    } catch (err: unknown) {
      toast(apiErrorMessage(err, 'Xóa danh mục thất bại. Danh mục có thể đang có sản phẩm.'), { tone: 'error' });
    }
  };

  return (
    <>
      <PageHeading
        eyebrow="Quản trị / Danh mục"
        title="Nhu cầu sử dụng"
        sub="Phân loại các dòng laptop để khách tìm đúng chiếc máy."
        actions={<button type="button" className="btn sm primary" onClick={() => openModal(null)}><Icon name="plus" /> Thêm danh mục</button>}
      />

      {loading ? <Spinner /> : (
        <div className="table-wrap">
          <table className="data-table">
            <thead><tr><th>ID</th><th>Tên danh mục</th><th>Mô tả</th><th>Trạng thái</th><th style={{ textAlign: 'right' }}>Thao tác</th></tr></thead>
            <tbody>
              {error ? <tr><td colSpan={5} className="table-empty">{error}</td></tr>
                : categories.length === 0 ? <tr><td colSpan={5} className="table-empty">Chưa có danh mục nào.</td></tr>
                : categories.map((cat) => (
                  <tr key={cat.id}>
                    <td className="muted">{cat.id}</td>
                    <td className="primary-cell">{cat.name}</td>
                    <td style={{ whiteSpace: 'normal', maxWidth: 360 }}>{cat.description || '—'}</td>
                    <td>{cat.active ? <Badge tone="green">Đang hiển thị</Badge> : <Badge>Tạm ẩn</Badge>}</td>
                    <td>
                      <div className="row" style={{ justifyContent: 'flex-end', gap: 8 }}>
                        <button type="button" className="btn sm" onClick={() => openModal(cat)}><Icon name="edit" /> Sửa</button>
                        <button type="button" className="btn sm danger" onClick={() => setDeleting(cat)}><Icon name="trash" /> Xóa</button>
                      </div>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal
        open={isModalOpen}
        title={editingCategory ? 'Sửa danh mục' : 'Thêm danh mục'}
        onClose={() => setIsModalOpen(false)}
        actions={<>
          <button type="button" className="btn" onClick={() => setIsModalOpen(false)} disabled={submitting}>Hủy</button>
          <button type="submit" form="category-form" className="btn primary" disabled={submitting}>{submitting ? 'Đang lưu...' : 'Lưu'}</button>
        </>}
      >
        <form id="category-form" className="stack" onSubmit={handleSubmit}>
          <label className="field"><span>Tên danh mục *</span>
            <input required value={name} onChange={(e) => setName(e.target.value)} placeholder="Ví dụ: Gaming, Mỏng nhẹ..." />
          </label>
          <label className="field"><span>Mô tả</span>
            <textarea value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Mô tả ngắn cho danh mục" style={{ minHeight: 80 }} />
          </label>
        </form>
      </Modal>

      <Modal
        open={!!deleting}
        title={`Xóa danh mục “${deleting?.name ?? ''}”?`}
        onClose={() => setDeleting(null)}
        actions={<>
          <button type="button" className="btn" onClick={() => setDeleting(null)}>Hủy</button>
          <button type="button" className="btn primary" onClick={() => void confirmDelete()}>Xóa</button>
        </>}
      >
        <p className="muted">Thao tác không thể hoàn tác.</p>
      </Modal>
    </>
  );
};

export default AdminCategoryPage;
