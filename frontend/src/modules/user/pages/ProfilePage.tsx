import { useAuth } from '@/app/providers/AuthProvider';
import { apiErrorMessage, Badge, EmptyState, Icon, Modal, Spinner, useToast } from '@/components/ui';
import React, { useCallback, useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { userApi } from '../api/userApi';
import { AccountLayout } from '../components/AccountLayout';
import { Address, CreateAddressRequest } from '../types';

const ROLE_LABEL: Record<string, string> = { ROLE_USER: 'Khách hàng', ROLE_STAFF: 'Nhân viên', ROLE_ADMIN: 'Quản trị viên' };
const EMPTY_ADDRESS: CreateAddressRequest = { recipientName: '', phone: '', street: '', ward: '', district: '', city: '', postalCode: '', isDefault: false };

const ProfileForm: React.FC = () => {
  const { user, refreshProfile } = useAuth();
  const toast = useToast();
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setFullName(user?.fullName || '');
    setPhone(user?.phone || '');
  }, [user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName.trim()) {
      toast('Họ và tên không được để trống.', { tone: 'error' });
      return;
    }
    try {
      setSaving(true);
      await userApi.updateProfile({ fullName: fullName.trim(), phone: phone.trim() || undefined });
      await refreshProfile();
      toast('Đã lưu hồ sơ.');
    } catch (err) {
      toast(apiErrorMessage(err, 'Không thể cập nhật thông tin. Vui lòng thử lại.'), { tone: 'error' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <form className="panel stack" onSubmit={handleSubmit}>
      <div className="row wrap">
        <div>
          <h3>{user?.fullName || 'Khách hàng LaptopStore'}</h3>
          <small className="muted">Thành viên từ {user?.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : '—'}</small>
        </div>
        <div className="row wrap push gap8">
          {user?.roles.map((r) => <Badge key={r} tone={r === 'ROLE_USER' ? 'neutral' : 'orange'}>{ROLE_LABEL[r] ?? r}</Badge>)}
          <Badge tone="green">{user?.status === 'ACTIVE' || !user?.status ? 'Đang hoạt động' : user.status}</Badge>
        </div>
      </div>
      <hr className="divider" style={{ margin: 0 }} />
      <div className="field-row">
        <label className="field"><span>Họ và tên</span>
          <input required value={fullName} onChange={(e) => setFullName(e.target.value)} autoComplete="name" />
        </label>
        <label className="field"><span>Số điện thoại</span>
          <input type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} autoComplete="tel" placeholder="0901234567" />
        </label>
      </div>
      <label className="field"><span>Email đăng nhập</span>
        <input type="email" value={user?.email || ''} readOnly />
        <small>Email được dùng để đăng nhập và nhận thông tin đơn hàng.</small>
      </label>
      <div className="form-actions">
        <button type="submit" className="btn primary" disabled={saving}>{saving ? 'Đang lưu...' : 'Lưu hồ sơ'}</button>
      </div>
    </form>
  );
};

const AddressBook: React.FC = () => {
  const toast = useToast();
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState<CreateAddressRequest>(EMPTY_ADDRESS);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState<Address | null>(null);

  const load = useCallback(async () => {
    try {
      setLoading(true);
      setAddresses(await userApi.getAddresses());
    } catch {
      toast('Không tải được sổ địa chỉ.', { tone: 'error' });
    } finally {
      setLoading(false);
    }
  }, [toast]);

  useEffect(() => { void load(); }, [load]);

  const set = (key: keyof CreateAddressRequest) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((f) => ({ ...f, [key]: key === 'isDefault' ? e.target.checked : e.target.value }));

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    if (!form.recipientName.trim() || !form.phone.trim() || !form.street.trim() || !form.city.trim()) {
      setFormError('Vui lòng điền đầy đủ các trường bắt buộc (*).');
      return;
    }
    const trimmed = (v?: string) => (v && v.trim() ? v.trim() : undefined);
    try {
      setSaving(true);
      await userApi.createAddress({
        recipientName: form.recipientName.trim(),
        phone: form.phone.trim(),
        street: form.street.trim(),
        city: form.city.trim(),
        ward: trimmed(form.ward),
        district: trimmed(form.district),
        postalCode: trimmed(form.postalCode),
        isDefault: form.isDefault || addresses.length === 0,
      });
      setModalOpen(false);
      setForm(EMPTY_ADDRESS);
      toast('Đã lưu địa chỉ.');
      await load();
    } catch (err) {
      setFormError(apiErrorMessage(err, 'Lỗi khi thêm địa chỉ. Vui lòng thử lại.'));
    } finally {
      setSaving(false);
    }
  };

  const confirmDelete = async () => {
    if (!deleting) return;
    try {
      await userApi.deleteAddress(deleting.id);
      setAddresses((prev) => prev.filter((a) => a.id !== deleting.id));
      toast('Đã xóa địa chỉ.');
    } catch (err) {
      toast(apiErrorMessage(err, 'Không thể xóa địa chỉ.'), { tone: 'error' });
    } finally {
      setDeleting(null);
    }
  };

  return (
    <>
      <div className="row between" style={{ marginBottom: 16 }}>
        <span className="muted small">{addresses.length} địa chỉ đã lưu</span>
        <button type="button" className="btn primary sm" onClick={() => { setForm(EMPTY_ADDRESS); setFormError(null); setModalOpen(true); }}>
          <Icon name="plus" /> Thêm địa chỉ
        </button>
      </div>

      {loading ? <Spinner /> : addresses.length === 0 ? (
        <EmptyState icon="home" title="Chưa có địa chỉ nhận hàng" copy="Thêm địa chỉ để thanh toán nhanh hơn trong lần mua tiếp theo.">
          <button type="button" className="btn primary" onClick={() => setModalOpen(true)}>Thêm địa chỉ đầu tiên</button>
        </EmptyState>
      ) : (
        <div className="stack" style={{ gap: 14 }}>
          {addresses.map((addr) => (
            <article className="panel row between wrap" key={addr.id} style={addr.isDefault ? { borderColor: '#f2ceb8' } : undefined}>
              <div style={{ minWidth: 0 }}>
                <div className="row wrap gap8">
                  <strong>{addr.recipientName}</strong>
                  <span className="muted small">{addr.phone}</span>
                  {addr.isDefault && <Badge tone="orange">Mặc định</Badge>}
                </div>
                <p className="small muted" style={{ marginTop: 6 }}>
                  {[addr.street, addr.ward, addr.district, addr.city, addr.postalCode].filter(Boolean).join(', ')}
                </p>
              </div>
              <button type="button" className="btn sm danger" onClick={() => setDeleting(addr)}>
                <Icon name="trash" /> Xóa
              </button>
            </article>
          ))}
        </div>
      )}

      <Modal
        open={modalOpen}
        title="Thêm địa chỉ nhận hàng"
        onClose={() => setModalOpen(false)}
        wide
        actions={<>
          <button type="button" className="btn" onClick={() => setModalOpen(false)}>Hủy</button>
          <button type="submit" form="address-form" className="btn primary" disabled={saving}>{saving ? 'Đang lưu...' : 'Lưu địa chỉ'}</button>
        </>}
      >
        <form id="address-form" className="stack" onSubmit={handleCreate}>
          {formError && <div className="notice red"><Icon name="alert" /><div>{formError}</div></div>}
          <div className="field-row">
            <label className="field"><span>Họ tên người nhận *</span><input required value={form.recipientName} onChange={set('recipientName')} autoComplete="name" /></label>
            <label className="field"><span>Số điện thoại *</span><input type="tel" required value={form.phone} onChange={set('phone')} autoComplete="tel" /></label>
          </div>
          <label className="field"><span>Số nhà, tên đường *</span><input required value={form.street} onChange={set('street')} placeholder="273 An Dương Vương" autoComplete="address-line1" /></label>
          <div className="field-row">
            <label className="field"><span>Phường / Xã</span><input value={form.ward} onChange={set('ward')} /></label>
            <label className="field"><span>Quận / Huyện</span><input value={form.district} onChange={set('district')} /></label>
          </div>
          <div className="field-row">
            <label className="field"><span>Tỉnh / Thành phố *</span><input required value={form.city} onChange={set('city')} placeholder="TP. Hồ Chí Minh" /></label>
            <label className="field"><span>Mã bưu chính</span><input value={form.postalCode} onChange={set('postalCode')} /></label>
          </div>
          <label className="check-label"><input type="checkbox" checked={!!form.isDefault} onChange={set('isDefault')} />Đặt làm địa chỉ mặc định</label>
        </form>
      </Modal>

      <Modal
        open={!!deleting}
        title="Xóa địa chỉ này?"
        onClose={() => setDeleting(null)}
        actions={<>
          <button type="button" className="btn" onClick={() => setDeleting(null)}>Hủy</button>
          <button type="button" className="btn primary" onClick={() => void confirmDelete()}>Xóa địa chỉ</button>
        </>}
      >
        <p className="muted">Địa chỉ của {deleting?.recipientName} sẽ được xóa khỏi sổ địa chỉ.</p>
      </Modal>
    </>
  );
};

/** /profile: hồ sơ cá nhân; /me/addresses: sổ địa chỉ. */
export const ProfilePage: React.FC = () => {
  const { pathname } = useLocation();
  const isAddresses = pathname.startsWith('/me/addresses');
  return (
    <AccountLayout title={isAddresses ? 'Sổ địa chỉ' : 'Hồ sơ của tôi'} sub={isAddresses ? 'Quản lý địa chỉ nhận hàng cho những lần mua sau.' : undefined}>
      {isAddresses ? <AddressBook /> : <ProfileForm />}
    </AccountLayout>
  );
};
