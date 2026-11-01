import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '@/app/providers/AuthProvider';
import { Icon, PageHeading, useToast } from '@/components/ui';

const initials = (name?: string, email?: string) => {
  const parts = (name || email || 'LS').trim().split(/\s+/).filter(Boolean);
  return (parts.length > 1 ? parts[parts.length - 2][0] + parts[parts.length - 1][0] : parts[0].slice(0, 2)).toUpperCase();
};

/** Khung trang tài khoản theo design (.account-layout + .account-nav). */
export const AccountLayout: React.FC<{ title: string; sub?: string; actions?: React.ReactNode; children: React.ReactNode }> = ({ title, sub, actions, children }) => {
  const { user, logout } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      toast('Bạn đã đăng xuất.');
      navigate('/');
    }
  };

  const link = (to: string, label: string, icon: Parameters<typeof Icon>[0]['name']) => (
    <NavLink to={to} end className={({ isActive }) => (isActive ? 'active' : '')}>
      <Icon name={icon} />{label}
    </NavLink>
  );

  return (
    <div className="ls-container section">
      <PageHeading eyebrow="Tài khoản của tôi" title={title} sub={sub ?? 'Thông tin chính xác, trải nghiệm thuận tiện hơn.'} actions={actions} />
      <div className="account-layout">
        <aside className="account-nav">
          <div className="account-id">
            <div className="avatar">{initials(user?.fullName, user?.email)}</div>
            <strong style={{ display: 'block', overflowWrap: 'anywhere' }}>{user?.fullName || 'Khách hàng'}</strong>
            <small className="muted" style={{ display: 'block', marginTop: 4, overflowWrap: 'anywhere' }}>{user?.email}</small>
          </div>
          <nav aria-label="Tài khoản">
            {link('/profile', 'Hồ sơ', 'user')}
            {link('/me/addresses', 'Sổ địa chỉ', 'home')}
            {link('/orders', 'Đơn hàng', 'orders')}
            {link('/cart', 'Giỏ hàng', 'cart')}
            {link('/ai-assistant', 'Tư vấn AI', 'sparkles')}
            <button type="button" onClick={() => void handleLogout()}><Icon name="logout" />Đăng xuất</button>
          </nav>
        </aside>
        <div style={{ minWidth: 0 }}>{children}</div>
      </div>
    </div>
  );
};
