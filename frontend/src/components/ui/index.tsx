import React from 'react';
import { Link } from 'react-router-dom';
import { Icon, IconName } from './Icon';

export { Icon } from './Icon';
export type { IconName } from './Icon';
export { Modal } from './Modal';
export { ProductImage, placeholderFor, PLACEHOLDER_DARK, PLACEHOLDER_SILVER } from './ProductImage';
export { ToastProvider, useToast } from './Toast';

export const formatMoney = (value?: number | null) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(Number(value) || 0);

export const formatDateTime = (iso?: string | null) =>
  iso ? new Date(iso).toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'short' }) : '—';

/** Lấy thông báo lỗi từ response backend (message) hoặc dùng câu mặc định. */
export const apiErrorMessage = (err: unknown, fallback: string) => {
  const data = (err as { response?: { data?: unknown } })?.response?.data;
  if (typeof data === 'string' && data.trim() && data.length < 300) return data;
  const message = (data as { message?: string; error?: string } | undefined)?.message;
  return message && message.trim() ? message : fallback;
};

export const Brand: React.FC<{ to?: string }> = ({ to = '/' }) => (
  <Link to={to} className="brand" aria-label="LaptopStore — về trang chủ">
    <span className="brand-mark"><Icon name="laptop" /></span>
    laptopstore
  </Link>
);

export type BadgeTone = 'neutral' | 'orange' | 'green' | 'blue' | 'red';

export const Badge: React.FC<{ tone?: BadgeTone; children: React.ReactNode }> = ({ tone = 'neutral', children }) => (
  <span className={`badge ${tone}`}><span className="dot" />{children}</span>
);

export const PageHeading: React.FC<{ eyebrow?: string; title: string; sub?: string; actions?: React.ReactNode }> = ({ eyebrow, title, sub, actions }) => (
  <div className="page-heading">
    <div>
      {eyebrow && <div className="eyebrow">{eyebrow}</div>}
      <h1>{title}</h1>
      {sub && <p>{sub}</p>}
    </div>
    {actions && <div className="row wrap">{actions}</div>}
  </div>
);

interface EmptyStateProps {
  icon?: IconName;
  title: string;
  copy?: string;
  action?: { label: string; to: string };
  children?: React.ReactNode;
}

export const EmptyState: React.FC<EmptyStateProps> = ({ icon = 'box', title, copy, action, children }) => (
  <div className="empty">
    <div className="empty-icon"><Icon name={icon} /></div>
    <h2>{title}</h2>
    {copy && <p>{copy}</p>}
    {action && <Link to={action.to} className="btn primary">{action.label}</Link>}
    {children}
  </div>
);

export const Spinner: React.FC<{ label?: string }> = ({ label = 'Đang tải...' }) => (
  <div role="status" aria-live="polite">
    <div className="spinner" />
    <span className="sr-only">{label}</span>
  </div>
);

/** Phân trang 0-based, khớp Page của Spring Data. */
export const Pager: React.FC<{ page: number; totalPages: number; onChange: (page: number) => void }> = ({ page, totalPages, onChange }) => {
  if (totalPages <= 1) return null;
  const start = Math.max(0, Math.min(page - 2, totalPages - 5));
  const pages = Array.from({ length: Math.min(5, totalPages) }, (_, i) => start + i);
  return (
    <nav className="pagination" aria-label="Phân trang">
      <button type="button" className="btn sm" disabled={page === 0} onClick={() => onChange(page - 1)} aria-label="Trang trước">
        <Icon name="chevron-left" />
      </button>
      {pages.map((p) => (
        <button key={p} type="button" className={`btn sm ${p === page ? 'active' : ''}`} aria-current={p === page ? 'page' : undefined} onClick={() => onChange(p)}>
          {p + 1}
        </button>
      ))}
      <button type="button" className="btn sm" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)} aria-label="Trang sau">
        <Icon name="chevron-right" />
      </button>
    </nav>
  );
};

export const ORDER_STATUS: Record<string, { label: string; tone: BadgeTone }> = {
  CREATED: { label: 'Mới tạo', tone: 'neutral' },
  PENDING_PAYMENT: { label: 'Chờ thanh toán', tone: 'orange' },
  WAITING_CONFIRMATION: { label: 'Chờ xác nhận', tone: 'orange' },
  CONFIRMED: { label: 'Đã xác nhận', tone: 'blue' },
  PACKING: { label: 'Đang chuẩn bị', tone: 'blue' },
  PACKED: { label: 'Đang chuẩn bị', tone: 'blue' },
  SHIPPING: { label: 'Đang giao', tone: 'blue' },
  DELIVERED: { label: 'Đã giao', tone: 'green' },
  DELIVERY_FAILED: { label: 'Giao thất bại', tone: 'red' },
  CANCELLED: { label: 'Đã hủy', tone: 'red' },
  RETURNED: { label: 'Đã trả hàng', tone: 'neutral' },
};

export const OrderStatusBadge: React.FC<{ status: string }> = ({ status }) => {
  const s = ORDER_STATUS[status] ?? { label: status, tone: 'neutral' as BadgeTone };
  return <Badge tone={s.tone}>{s.label}</Badge>;
};
