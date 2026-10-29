import { CouponUsageView, CouponView } from '../types';

// Dữ liệu giả lập trong bộ nhớ, dùng tạm cho UI khi chưa nối API (xem api/couponApi.ts)
let coupons: CouponView[] = [
    {
        id: 1,
        code: 'LAPTOP500',
        description: 'Giảm trực tiếp cho đơn hàng từ 20 triệu đồng.',
        discountType: 'FIXED',
        discountValue: 500000,
        maxDiscount: 500000,
        minOrderAmount: 20000000,
        usageLimit: 200,
        usagePerUser: 1,
        startsAt: '2026-10-19T17:00:00Z',
        endsAt: '2026-10-31T16:59:00Z',
        active: true,
        usedCount: 42,
        createdAt: '2026-10-20T02:00:00Z',
        updatedAt: '2026-10-24T03:00:00Z'
    },
    {
        id: 2,
        code: 'ROG10',
        description: 'Giảm 10% cho dòng laptop gaming ROG.',
        discountType: 'PERCENT',
        discountValue: 10,
        maxDiscount: 1000000,
        minOrderAmount: 25000000,
        usageLimit: 100,
        usagePerUser: 1,
        startsAt: '2026-10-23T17:00:00Z',
        endsAt: '2026-10-31T16:59:00Z',
        active: true,
        usedCount: 37,
        createdAt: '2026-10-22T02:00:00Z',
        updatedAt: '2026-10-23T08:30:00Z'
    },
    {
        id: 3,
        code: 'WELCOME300',
        description: 'Ưu đãi chào mừng khách hàng mới.',
        discountType: 'FIXED',
        discountValue: 300000,
        maxDiscount: 300000,
        minOrderAmount: 15000000,
        usageLimit: 500,
        usagePerUser: 1,
        startsAt: '2026-09-30T17:00:00Z',
        endsAt: '2026-10-31T16:59:00Z',
        active: true,
        usedCount: 107,
        createdAt: '2026-09-28T02:00:00Z',
        updatedAt: '2026-10-01T01:00:00Z'
    },
    {
        id: 4,
        code: 'BACKTOSCHOOL',
        description: 'Mùa tựu trường – giảm 5% cho học sinh, sinh viên.',
        discountType: 'PERCENT',
        discountValue: 5,
        maxDiscount: 800000,
        minOrderAmount: 12000000,
        usageLimit: 300,
        usagePerUser: 1,
        startsAt: '2026-08-31T17:00:00Z',
        endsAt: '2026-09-30T16:59:00Z',
        active: true,
        usedCount: 214,
        createdAt: '2026-08-25T02:00:00Z',
        updatedAt: '2026-09-30T17:00:00Z'
    }
];

const lastNames = ['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Võ', 'Đặng', 'Bùi', 'Đỗ', 'Huỳnh'];
const firstNames = ['Minh Tuấn', 'Thảo Nguyên', 'Quốc Huy', 'Gia Hân', 'Hoàng Nam', 'Thanh Tâm', 'Khánh Linh', 'Đức Anh', 'Mai Phương', 'Trọng Nghĩa', 'Bảo Ngọc'];

const pad = (n: number, len = 2) => String(n).padStart(len, '0');

// Sinh lượt dùng có tính quy luật (không random) để dữ liệu ổn định giữa các lần reload
const generateUsages = (couponId: number, code: string, amount: number, count: number, from: Date, stepHours: number, startId: number, startOrder: number): CouponUsageView[] =>
    Array.from({ length: count }, (_, i) => {
        const createdAt = new Date(from.getTime() - i * stepHours * 3600 * 1000);
        const orderNo = startOrder - i;
        const yymmdd = `${String(createdAt.getFullYear()).slice(2)}${pad(createdAt.getMonth() + 1)}${pad(createdAt.getDate())}`;
        return {
            id: startId + i,
            couponId,
            couponCode: code,
            userId: 200 + startId + i,
            orderId: orderNo,
            orderCode: `LS-${yymmdd}-${pad(orderNo, 4)}`,
            customerName: `${lastNames[(i * 3 + couponId) % lastNames.length]} ${firstNames[(i * 7 + couponId) % firstNames.length]}`,
            discountAmount: amount,
            status: 'ACTIVE',
            createdAt: createdAt.toISOString()
        };
    });

let usages: CouponUsageView[] = [
    { id: 1, couponId: 1, couponCode: 'LAPTOP500', userId: 101, orderId: 841, orderCode: 'LS-261024-0841', customerName: 'Nguyễn Minh Tuấn', discountAmount: 500000, status: 'ACTIVE', createdAt: '2026-10-24T03:42:00Z' },
    { id: 2, couponId: 1, couponCode: 'LAPTOP500', userId: 102, orderId: 836, orderCode: 'LS-261024-0836', customerName: 'Trần Thảo Nguyên', discountAmount: 500000, status: 'ACTIVE', createdAt: '2026-10-24T02:25:00Z' },
    { id: 3, couponId: 1, couponCode: 'LAPTOP500', userId: 103, orderId: 819, orderCode: 'LS-261023-0819', customerName: 'Lê Quốc Huy', discountAmount: 500000, status: 'ACTIVE', createdAt: '2026-10-23T09:18:00Z' },
    { id: 4, couponId: 1, couponCode: 'LAPTOP500', userId: 104, orderId: 811, orderCode: 'LS-261023-0811', customerName: 'Phạm Gia Hân', discountAmount: 500000, status: 'RELEASED', createdAt: '2026-10-23T05:05:00Z' },
    { id: 5, couponId: 1, couponCode: 'LAPTOP500', userId: 105, orderId: 796, orderCode: 'LS-261022-0796', customerName: 'Nguyễn Hoàng Nam', discountAmount: 500000, status: 'ACTIVE', createdAt: '2026-10-22T08:45:00Z' },
    // usedCount của từng mã = số lượt ACTIVE bên dưới
    ...generateUsages(1, 'LAPTOP500', 500000, 38, new Date('2026-10-22T05:10:00Z'), 1.2, 100, 790),
    ...generateUsages(2, 'ROG10', 1000000, 37, new Date('2026-10-28T10:20:00Z'), 2.5, 200, 980),
    ...generateUsages(3, 'WELCOME300', 300000, 107, new Date('2026-10-28T13:00:00Z'), 6, 300, 1200),
    ...generateUsages(4, 'BACKTOSCHOOL', 800000, 214, new Date('2026-09-30T14:00:00Z'), 3, 500, 700)
];

export const couponMockStore = {
    getCoupons: (): CouponView[] => [...coupons],

    getCouponById: (id: number): CouponView | undefined => coupons.find(c => c.id === id),

    isCodeTaken: (code: string, excludeId?: number): boolean =>
        coupons.some(c => c.code.toUpperCase() === code.toUpperCase() && c.id !== excludeId),

    saveCoupon: (coupon: Omit<CouponView, 'id' | 'usedCount' | 'createdAt' | 'updatedAt'> & { id?: number }): CouponView => {
        const now = new Date().toISOString();
        const existing = coupon.id ? coupons.find(c => c.id === coupon.id) : undefined;
        if (existing) {
            const updated = { ...existing, ...coupon, id: existing.id, updatedAt: now };
            coupons = coupons.map(c => (c.id === existing.id ? updated : c));
            return updated;
        }
        const created: CouponView = {
            ...coupon,
            id: Math.max(0, ...coupons.map(c => c.id)) + 1,
            usedCount: 0,
            createdAt: now,
            updatedAt: now
        };
        coupons = [created, ...coupons];
        return created;
    },

    setActive: (id: number, active: boolean): void => {
        coupons = coupons.map(c => (c.id === id ? { ...c, active, updatedAt: new Date().toISOString() } : c));
    },

    getAllUsages: (): CouponUsageView[] => [...usages],

    getUsagesByCoupon: (couponId: number): CouponUsageView[] =>
        usages
            .filter(u => u.couponId === couponId)
            .sort((a, b) => b.createdAt.localeCompare(a.createdAt)),

    releaseUsage: (usageId: number): void => {
        const usage = usages.find(u => u.id === usageId);
        if (!usage || usage.status === 'RELEASED') return;
        usages = usages.map(u => (u.id === usageId ? { ...u, status: 'RELEASED' } : u));
        coupons = coupons.map(c => (c.id === usage.couponId ? { ...c, usedCount: Math.max(0, c.usedCount - 1) } : c));
    }
};
