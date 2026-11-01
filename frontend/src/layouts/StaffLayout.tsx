import { useAuth } from '@/app/providers/AuthProvider';
import { Brand, Icon, IconName, useToast } from '@/components/ui';
import { ROLES } from '@/constants/roles';
import React, { useEffect, useState } from 'react';
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';

type NavItem = { to: string; label: string; icon: IconName; end?: boolean };

const STAFF_NAV: NavItem[] = [
  { to: '/staff/dashboard', label: 'Tổng quan', icon: 'grid' },
  { to: '/staff/orders', label: 'Đơn hàng', icon: 'orders' },
  { to: '/staff/inventory', label: 'Kho hàng', icon: 'box' },
  { to: '/staff/procurement', label: 'Nhập hàng', icon: 'truck' },
  { to: '/staff/suppliers', label: 'Nhà cung cấp', icon: 'users' },
  { to: '/staff/reviews', label: 'Đánh giá', icon: 'star' },
];

const ADMIN_NAV: NavItem[] = [
  { to: '/admin', label: 'Tổng quan', icon: 'grid', end: true },
  { to: '/admin/products', label: 'Sản phẩm', icon: 'laptop' },
  { to: '/admin/categories', label: 'Danh mục', icon: 'tag' },
  { to: '/admin/promotions', label: 'Khuyến mãi', icon: 'wallet' },
  ...STAFF_NAV.filter((i) => i.to !== '/staff/dashboard'),
];

const initials = (name?: string, email?: string) => {
  const parts = (name || email || 'LS').trim().split(/\s+/).filter(Boolean);
  return (parts.length > 1 ? parts[parts.length - 2][0] + parts[parts.length - 1][0] : parts[0].slice(0, 2)).toUpperCase();
};

/** Không gian làm việc Staff/Admin theo design (.ops / .sidebar / .ops-header). */
export const StaffLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const isAdmin = !!user?.roles.includes(ROLES.ADMIN);
  const nav = isAdmin ? ADMIN_NAV : STAFF_NAV;

  useEffect(() => setSidebarOpen(false), [location.pathname]);

  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      toast('Bạn đã đăng xuất.');
      navigate('/login');
    }
  };

  return (
    <div className="ops">
      <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`} aria-label="Điều hướng quản trị">
        <Brand to={isAdmin ? '/admin' : '/staff/dashboard'} />
        <p className="nav-label">{isAdmin ? 'Quản trị hệ thống' : 'Vận hành cửa hàng'}</p>
        <nav>
          {nav.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end} className={({ isActive }) => (isActive ? 'active' : '')}>
              <Icon name={item.icon} /><span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="sidebar-help">
            <Icon name="headset" />
            <strong style={{ display: 'block' }}>Một ngày thật hiệu quả</strong>
            <p>Mọi thao tác, cùng một nơi.</p>
          </div>
          <Link to="/"><Icon name="arrow-left" /> Về cửa hàng</Link>
          <button type="button" onClick={() => void handleLogout()}><Icon name="logout" /> Đăng xuất</button>
        </div>
      </aside>
      <div className="mobile-backdrop" onClick={() => setSidebarOpen(false)} aria-hidden="true" />

      <div className="ops-shell">
        <header className="ops-header">
          <button type="button" className="icon-btn mobile-only" aria-label="Mở menu" onClick={() => setSidebarOpen(true)}>
            <Icon name="menu" />
          </button>
          <span>{isAdmin ? 'Không gian quản trị' : 'Không gian nhân viên'}</span>
          <div className="push">
            <Link to="/staff/orders" className="icon-btn" aria-label="Đơn hàng cần xử lý"><Icon name="bell" /></Link>
            <div className="avatar" style={{ width: 36, height: 36 }}>{initials(user?.fullName, user?.email)}</div>
            <div className="user-name">
              {user?.fullName || user?.email}
              <div className="muted" style={{ fontWeight: 400, fontSize: 10 }}>{isAdmin ? 'Quản trị viên' : 'Nhân viên vận hành'}</div>
            </div>
          </div>
        </header>
        <main id="main" className="ops-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
