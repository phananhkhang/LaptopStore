import { Coupon, CouponDisplayStatus } from '../types';

const pad = (n: number) => String(n).padStart(2, '0');

export const formatVnd = (value: number | null | undefined): string =>
    value == null ? '—' : `${new Intl.NumberFormat('vi-VN').format(value)} đ`;

// 1000000 → "1 triệu", 800000 → "800k", 72800000 → "72,8 triệu"
export const formatShortMoney = (value: number): string => {
    if (value >= 1_000_000) {
        return `${new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 1 }).format(value / 1_000_000)} triệu`;
    }
    if (value >= 1_000) return `${Math.round(value / 1_000)}k`;
    return String(value);
};

export const formatDiscount = (coupon: Pick<Coupon, 'discountType' | 'discountValue' | 'maxDiscount'>): string => {
    if (coupon.discountType === 'FIXED') return formatVnd(coupon.discountValue);
    const max = coupon.maxDiscount ? ` · Tối đa ${formatShortMoney(coupon.maxDiscount)}` : '';
    return `${coupon.discountValue}%${max}`;
};

export const formatDate = (iso: string): string => {
    const d = new Date(iso);
    return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`;
};

export const formatDateTime = (iso: string): string => {
    const d = new Date(iso);
    return `${formatDate(iso)} · ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

// "20–31/10/2026" nếu cùng tháng, ngược lại "25/09–05/10/2026" hoặc đủ hai ngày
export const formatDateRange = (startIso: string, endIso: string): string => {
    const s = new Date(startIso);
    const e = new Date(endIso);
    if (s.getFullYear() !== e.getFullYear()) return `${formatDate(startIso)}–${formatDate(endIso)}`;
    if (s.getMonth() !== e.getMonth()) {
        return `${pad(s.getDate())}/${pad(s.getMonth() + 1)}–${formatDate(endIso)}`;
    }
    return `${pad(s.getDate())}–${formatDate(endIso)}`;
};

export const getDisplayStatus = (coupon: Pick<Coupon, 'active' | 'startsAt' | 'endsAt'>, now = new Date()): CouponDisplayStatus => {
    if (now > new Date(coupon.endsAt)) return 'ENDED';
    if (!coupon.active) return 'PAUSED';
    if (now < new Date(coupon.startsAt)) return 'UPCOMING';
    return 'RUNNING';
};

// Chuyển qua lại giữa ISO Instant và giá trị của <input type="datetime-local">
export const toDateTimeLocal = (iso: string): string => {
    if (!iso) return '';
    const d = new Date(iso);
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

export const fromDateTimeLocal = (value: string): string => (value ? new Date(value).toISOString() : '');

// "20.000.000" → 20000000
export const parseMoneyInput = (value: string): number => Number(value.replace(/\D/g, '')) || 0;
export const formatMoneyInput = (value: number): string => (value ? new Intl.NumberFormat('vi-VN').format(value) : '');
