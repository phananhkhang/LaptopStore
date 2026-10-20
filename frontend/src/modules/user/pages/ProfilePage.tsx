import React, { useEffect, useState } from 'react';
import { useAuth } from '@/app/providers/AuthProvider';
import { userApi } from '../api/userApi';
import { Address, CreateAddressRequest } from '../types';

export const ProfilePage: React.FC = () => {
  const { user, logout, refreshProfile } = useAuth();

  const [activeTab, setActiveTab] = useState<'profile' | 'addresses'>('profile');

  // Profile update form state
  const [fullName, setFullName] = useState<string>('');
  const [phone, setPhone] = useState<string>('');
  const [profileSaving, setProfileSaving] = useState<boolean>(false);
  const [profileMessage, setProfileMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Addresses state
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [loadingAddresses, setLoadingAddresses] = useState<boolean>(false);
  const [showAddAddress, setShowAddAddress] = useState<boolean>(false);
  const [addressSaving, setAddressSaving] = useState<boolean>(false);
  const [addressError, setAddressError] = useState<string | null>(null);

  // New Address form fields
  const [newRecipientName, setNewRecipientName] = useState<string>('');
  const [newPhone, setNewPhone] = useState<string>('');
  const [newStreet, setNewStreet] = useState<string>('');
  const [newWard, setNewWard] = useState<string>('');
  const [newDistrict, setNewDistrict] = useState<string>('');
  const [newCity, setNewCity] = useState<string>('');
  const [newPostalCode, setNewPostalCode] = useState<string>('');
  const [newIsDefault, setNewIsDefault] = useState<boolean>(false);

  useEffect(() => {
    if (user) {
      setFullName(user.fullName || '');
      setPhone(user.phone || '');
    }
  }, [user]);

  const loadAddresses = async () => {
    try {
      setLoadingAddresses(true);
      const data = await userApi.getAddresses();
      setAddresses(data);
    } catch {
      // ignore
    } finally {
      setLoadingAddresses(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'addresses') {
      loadAddresses();
    }
  }, [activeTab]);

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setProfileMessage(null);
    try {
      setProfileSaving(true);
      await userApi.updateProfile({
        fullName: fullName.trim(),
        phone: phone.trim() ? phone.trim() : undefined,
      });
      await refreshProfile();
      setProfileMessage({ type: 'success', text: 'Cập nhật thông tin thành công!' });
    } catch {
      setProfileMessage({ type: 'error', text: 'Không thể cập nhật thông tin. Vui lòng thử lại.' });
    } finally {
      setProfileSaving(false);
    }
  };

  const handleCreateAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    setAddressError(null);

    if (!newRecipientName.trim() || !newPhone.trim() || !newStreet.trim() || !newCity.trim()) {
      setAddressError('Vui lòng điền đầy đủ các thông tin bắt buộc (*)');
      return;
    }

    try {
      setAddressSaving(true);
      const payload: CreateAddressRequest = {
        recipientName: newRecipientName.trim(),
        phone: newPhone.trim(),
        street: newStreet.trim(),
        ward: newWard.trim() ? newWard.trim() : undefined,
        district: newDistrict.trim() ? newDistrict.trim() : undefined,
        city: newCity.trim(),
        postalCode: newPostalCode.trim() ? newPostalCode.trim() : undefined,
        isDefault: newIsDefault,
      };

      await userApi.createAddress(payload);
      setShowAddAddress(false);
      // Reset form
      setNewRecipientName('');
      setNewPhone('');
      setNewStreet('');
      setNewWard('');
      setNewDistrict('');
      setNewCity('');
      setNewPostalCode('');
      setNewIsDefault(false);
      await loadAddresses();
    } catch {
      setAddressError('Lỗi khi thêm địa chỉ. Vui lòng thử lại.');
    } finally {
      setAddressSaving(false);
    }
  };

  const handleDeleteAddress = async (id: number) => {
    if (window.confirm('Bạn có chắc muốn xóa địa chỉ này?')) {
      try {
        await userApi.deleteAddress(id);
        setAddresses((prev) => prev.filter((a) => a.id !== id));
      } catch {
        alert('Không thể xóa địa chỉ. Vui lòng thử lại.');
      }
    }
  };

  return (
    <div style={{ maxWidth: 900, margin: '2rem auto', padding: '0 1rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 700, color: '#0f172a', margin: 0 }}>
            Hồ Sơ Tài Khoản
          </h1>
          <p style={{ color: '#64748b', margin: '0.25rem 0 0', fontSize: '0.9375rem' }}>
            Quản lý thông tin cá nhân và sổ địa chỉ giao nhận hàng
          </p>
        </div>
        <button
          onClick={() => logout()}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#f1f5f9',
            color: '#ef4444',
            border: '1px solid #e2e8f0',
            borderRadius: 8,
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          Đăng xuất
        </button>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', borderBottom: '2px solid #e2e8f0', marginBottom: '2rem' }}>
        <button
          onClick={() => setActiveTab('profile')}
          style={{
            padding: '0.75rem 1.5rem',
            border: 'none',
            background: 'none',
            fontSize: '1rem',
            fontWeight: activeTab === 'profile' ? 700 : 500,
            color: activeTab === 'profile' ? '#2563eb' : '#64748b',
            borderBottom: activeTab === 'profile' ? '2px solid #2563eb' : 'none',
            marginBottom: -2,
            cursor: 'pointer',
          }}
        >
          Thông tin cá nhân
        </button>
        <button
          onClick={() => setActiveTab('addresses')}
          style={{
            padding: '0.75rem 1.5rem',
            border: 'none',
            background: 'none',
            fontSize: '1rem',
            fontWeight: activeTab === 'addresses' ? 700 : 500,
            color: activeTab === 'addresses' ? '#2563eb' : '#64748b',
            borderBottom: activeTab === 'addresses' ? '2px solid #2563eb' : 'none',
            marginBottom: -2,
            cursor: 'pointer',
          }}
        >
          Sổ địa chỉ nhận hàng
        </button>
      </div>

      {activeTab === 'profile' && (
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: 12,
          border: '1px solid #e2e8f0',
          padding: '2rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.05)',
        }}>
          {profileMessage && (
            <div style={{
              padding: '0.75rem 1rem',
              borderRadius: 8,
              marginBottom: '1.5rem',
              backgroundColor: profileMessage.type === 'success' ? '#f0fdf4' : '#fef2f2',
              border: `1px solid ${profileMessage.type === 'success' ? '#bbf7d0' : '#fecaca'}`,
              color: profileMessage.type === 'success' ? '#16a34a' : '#dc2626',
              fontSize: '0.875rem',
            }}>
              {profileMessage.text}
            </div>
          )}

          <form onSubmit={handleUpdateProfile}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem', marginBottom: '1.5rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: 6, fontWeight: 600, fontSize: '0.875rem', color: '#334155' }}>
                  Địa chỉ Email (Không thể đổi)
                </label>
                <input
                  type="email"
                  value={user?.email || ''}
                  disabled
                  style={{
                    width: '100%',
                    padding: '0.625rem 0.875rem',
                    backgroundColor: '#f8fafc',
                    border: '1px solid #e2e8f0',
                    borderRadius: 6,
                    color: '#64748b',
                    boxSizing: 'border-box',
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: 6, fontWeight: 600, fontSize: '0.875rem', color: '#334155' }}>
                  Vai trò (Role)
                </label>
                <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
                  {user?.roles?.map((role) => (
                    <span
                      key={role}
                      style={{
                        padding: '0.25rem 0.625rem',
                        backgroundColor: '#dbeafe',
                        color: '#1d4ed8',
                        borderRadius: 999,
                        fontSize: '0.75rem',
                        fontWeight: 700,
                      }}
                    >
                      {role}
                    </span>
                  ))}
                </div>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: 6, fontWeight: 600, fontSize: '0.875rem', color: '#334155' }}>
                  Họ và tên
                </label>
                <input
                  type="text"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.625rem 0.875rem',
                    border: '1px solid #cbd5e1',
                    borderRadius: 6,
                    boxSizing: 'border-box',
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: 6, fontWeight: 600, fontSize: '0.875rem', color: '#334155' }}>
                  Số điện thoại
                </label>
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.625rem 0.875rem',
                    border: '1px solid #cbd5e1',
                    borderRadius: 6,
                    boxSizing: 'border-box',
                  }}
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={profileSaving}
              style={{
                padding: '0.625rem 1.5rem',
                backgroundColor: profileSaving ? '#93c5fd' : '#2563eb',
                color: '#ffffff',
                border: 'none',
                borderRadius: 6,
                fontWeight: 600,
                cursor: profileSaving ? 'not-allowed' : 'pointer',
              }}
            >
              {profileSaving ? 'Đang lưu...' : 'Lưu thay đổi'}
            </button>
          </form>
        </div>
      )}

      {activeTab === 'addresses' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 600, color: '#1e293b', margin: 0 }}>
              Địa chỉ của tôi ({addresses.length})
            </h2>
            <button
              onClick={() => setShowAddAddress(!showAddAddress)}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: showAddAddress ? '#f1f5f9' : '#2563eb',
                color: showAddAddress ? '#334155' : '#ffffff',
                border: 'none',
                borderRadius: 6,
                fontWeight: 600,
                cursor: 'pointer',
              }}
            >
              {showAddAddress ? 'Hủy' : '+ Thêm địa chỉ mới'}
            </button>
          </div>

          {showAddAddress && (
            <div style={{
              backgroundColor: '#f8fafc',
              border: '1px solid #e2e8f0',
              borderRadius: 12,
              padding: '1.5rem',
              marginBottom: '2rem',
            }}>
              <h3 style={{ margin: '0 0 1rem', fontSize: '1.125rem' }}>Nhập thông tin địa chỉ mới</h3>
              {addressError && (
                <div style={{ color: '#dc2626', marginBottom: '1rem', fontSize: '0.875rem' }}>{addressError}</div>
              )}
              <form onSubmit={handleCreateAddress}>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: 4, fontSize: '0.8125rem', fontWeight: 600 }}>Họ tên người nhận *</label>
                    <input
                      type="text"
                      required
                      value={newRecipientName}
                      onChange={(e) => setNewRecipientName(e.target.value)}
                      style={{ width: '100%', padding: '0.5rem', borderRadius: 6, border: '1px solid #cbd5e1', boxSizing: 'border-box' }}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', marginBottom: 4, fontSize: '0.8125rem', fontWeight: 600 }}>Số điện thoại *</label>
                    <input
                      type="tel"
                      required
                      value={newPhone}
                      onChange={(e) => setNewPhone(e.target.value)}
                      style={{ width: '100%', padding: '0.5rem', borderRadius: 6, border: '1px solid #cbd5e1', boxSizing: 'border-box' }}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', marginBottom: 4, fontSize: '0.8125rem', fontWeight: 600 }}>Tỉnh / Thành phố *</label>
                    <input
                      type="text"
                      required
                      value={newCity}
                      onChange={(e) => setNewCity(e.target.value)}
                      placeholder="TP. Hồ Chí Minh"
                      style={{ width: '100%', padding: '0.5rem', borderRadius: 6, border: '1px solid #cbd5e1', boxSizing: 'border-box' }}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', marginBottom: 4, fontSize: '0.8125rem', fontWeight: 600 }}>Quận / Huyện</label>
                    <input
                      type="text"
                      value={newDistrict}
                      onChange={(e) => setNewDistrict(e.target.value)}
                      placeholder="Quận 1"
                      style={{ width: '100%', padding: '0.5rem', borderRadius: 6, border: '1px solid #cbd5e1', boxSizing: 'border-box' }}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', marginBottom: 4, fontSize: '0.8125rem', fontWeight: 600 }}>Phường / Xã</label>
                    <input
                      type="text"
                      value={newWard}
                      onChange={(e) => setNewWard(e.target.value)}
                      placeholder="Phường Bến Nghé"
                      style={{ width: '100%', padding: '0.5rem', borderRadius: 6, border: '1px solid #cbd5e1', boxSizing: 'border-box' }}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', marginBottom: 4, fontSize: '0.8125rem', fontWeight: 600 }}>Địa chỉ số nhà, tên đường *</label>
                    <input
                      type="text"
                      required
                      value={newStreet}
                      onChange={(e) => setNewStreet(e.target.value)}
                      placeholder="Số 10 Lê Duẩn"
                      style={{ width: '100%', padding: '0.5rem', borderRadius: 6, border: '1px solid #cbd5e1', boxSizing: 'border-box' }}
                    />
                  </div>
                </div>

                <div style={{ marginBottom: '1.25rem' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontSize: '0.875rem' }}>
                    <input
                      type="checkbox"
                      checked={newIsDefault}
                      onChange={(e) => setNewIsDefault(e.target.checked)}
                    />
                    Đặt làm địa chỉ nhận hàng mặc định
                  </label>
                </div>

                <button
                  type="submit"
                  disabled={addressSaving}
                  style={{
                    padding: '0.625rem 1.25rem',
                    backgroundColor: addressSaving ? '#93c5fd' : '#16a34a',
                    color: '#ffffff',
                    border: 'none',
                    borderRadius: 6,
                    fontWeight: 600,
                    cursor: addressSaving ? 'not-allowed' : 'pointer',
                  }}
                >
                  {addressSaving ? 'Đang lưu...' : 'Thêm địa chỉ'}
                </button>
              </form>
            </div>
          )}

          {loadingAddresses ? (
            <p style={{ color: '#64748b' }}>Đang tải danh sách địa chỉ...</p>
          ) : addresses.length === 0 ? (
            <div style={{
              textAlign: 'center',
              padding: '3rem',
              backgroundColor: '#ffffff',
              borderRadius: 12,
              border: '1px solid #e2e8f0',
              color: '#64748b',
            }}>
              Bạn chưa có địa chỉ nhận hàng nào. Hãy nhấn "+ Thêm địa chỉ mới" để thêm!
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {addresses.map((addr) => (
                <div
                  key={addr.id}
                  style={{
                    backgroundColor: '#ffffff',
                    border: addr.isDefault ? '2px solid #3b82f6' : '1px solid #e2e8f0',
                    borderRadius: 12,
                    padding: '1.25rem 1.5rem',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.375rem' }}>
                      <strong style={{ fontSize: '1rem', color: '#0f172a' }}>{addr.recipientName}</strong>
                      <span style={{ color: '#64748b', fontSize: '0.875rem' }}>{addr.phone}</span>
                      {addr.isDefault && (
                        <span style={{
                          fontSize: '0.6875rem',
                          fontWeight: 700,
                          backgroundColor: '#dbeafe',
                          color: '#1d4ed8',
                          padding: '0.125rem 0.5rem',
                          borderRadius: 4,
                        }}>
                          Mặc định
                        </span>
                      )}
                    </div>
                    <p style={{ margin: 0, color: '#475467', fontSize: '0.875rem' }}>
                      {[addr.street, addr.ward, addr.district, addr.city, addr.postalCode].filter(Boolean).join(', ')}
                    </p>
                  </div>
                  <div>
                    <button
                      onClick={() => handleDeleteAddress(addr.id)}
                      style={{
                        padding: '0.375rem 0.75rem',
                        border: '1px solid #fecaca',
                        backgroundColor: '#fff5f5',
                        color: '#dc2626',
                        borderRadius: 6,
                        fontSize: '0.8125rem',
                        cursor: 'pointer',
                      }}
                    >
                      Xóa
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
