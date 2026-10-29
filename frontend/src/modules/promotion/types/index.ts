// Khớp với các DTO trong backend: org.akira.laptopstore.promotion.dto
export type DiscountType = 'PERCENT' | 'FIXED';
export type CouponUsageStatus = 'ACTIVE' | 'RELEASED';

export interface Coupon {
    id: number;
    code: string;
    discountType: DiscountType;
    discountValue: number;
    maxDiscount: number | null;
    minOrderAmount: number | null;
    usageLimit: number | null;
    usagePerUser: number | null;
    startsAt: string; // ISO Instant
    endsAt: string;   // ISO Instant
    active: boolean;
}

export interface CouponUsage {
    id: number;
    couponId: number;
    couponCode: string;
    userId: number;
    orderId: number;
    discountAmount: number;
    status: CouponUsageStatus;
    createdAt: string;
}

export interface CreateCouponRequest {
    code: string;
    discountType: DiscountType;
    discountValue: number;
    maxDiscount?: number | null;
    minOrderAmount?: number | null;
    usageLimit?: number | null;
    usagePerUser?: number | null;
    startsAt?: string;
    endsAt?: string;
}

export type UpdateCouponRequest = CreateCouponRequest;

// ===== Kiểu dữ liệu chỉ dùng cho UI =====

// Trạng thái hiển thị, suy ra từ active + thời gian áp dụng
export type CouponDisplayStatus = 'RUNNING' | 'UPCOMING' | 'ENDED' | 'PAUSED';

export interface CouponView extends Coupon {
    description?: string;
    usedCount: number;
    createdAt: string;
    updatedAt: string;
}

// Backend chỉ trả userId/orderId, UI cần thêm mã đơn và tên khách để hiển thị
export interface CouponUsageView extends CouponUsage {
    orderCode: string;
    customerName: string;
}
