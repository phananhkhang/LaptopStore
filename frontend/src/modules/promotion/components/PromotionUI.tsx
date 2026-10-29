import React from 'react';
import { Badge, badgeTone } from '@/components/common/AdminUI';
import { CouponDisplayStatus, CouponUsageStatus } from '../types';

// Các component chung (nút, bảng, phân trang...) nằm ở components/common/AdminUI
export * from '@/components/common/AdminUI';

export const couponStatusLabel: Record<CouponDisplayStatus, string> = {
    RUNNING: 'Đang hoạt động',
    UPCOMING: 'Sắp bắt đầu',
    ENDED: 'Đã kết thúc',
    PAUSED: 'Tạm ngưng'
};

const couponStatusTone: Record<CouponDisplayStatus, keyof typeof badgeTone> = {
    RUNNING: 'green',
    UPCOMING: 'blue',
    ENDED: 'gray',
    PAUSED: 'orange'
};

export const CouponStatusBadge: React.FC<{ status: CouponDisplayStatus }> = ({ status }) => (
    <Badge tone={couponStatusTone[status]}>{couponStatusLabel[status]}</Badge>
);

export const UsageStatusBadge: React.FC<{ status: CouponUsageStatus }> = ({ status }) =>
    status === 'ACTIVE' ? <Badge tone="green">Đã áp dụng</Badge> : <Badge tone="gray">Đã hoàn lượt</Badge>;
