import { ReviewableOrderItem, ReviewSummary, ReviewView } from '../types';

// Dữ liệu giả lập trong bộ nhớ, dùng tạm cho UI khi chưa nối API (xem api/reviewApi.ts)
const products = [
    { productId: 1, variantId: 11, productName: 'ASUS ROG Strix G15', variantName: '16 GB RAM · 512 GB SSD · Đen', sku: 'ROG-G15-01' },
    { productId: 2, variantId: 21, productName: 'ThinkPad E14', variantName: '16 GB RAM · 512 GB SSD · Đen', sku: 'TP-E14-02' },
    { productId: 3, variantId: 31, productName: 'MacBook Air M3', variantName: '8 GB RAM · 256 GB SSD · Bạc', sku: 'MBA-M3-01' },
    { productId: 4, variantId: 41, productName: 'Dell XPS 13', variantName: '16 GB RAM · 512 GB SSD · Bạc', sku: 'XPS-13-01' }
];

const lastNames = ['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Võ', 'Đặng', 'Bùi', 'Đỗ', 'Huỳnh'];
const firstNames = ['Minh An', 'Thu Hà', 'Quốc Huy', 'Gia Hân', 'Hoàng Nam', 'Thanh Tâm', 'Khánh Linh', 'Đức Anh', 'Mai Phương', 'Bảo Ngọc', 'Trọng Nghĩa'];

const comments: Record<number, string[]> = {
    5: [
        'Máy chạy mượt, màn hình đẹp. Đóng gói kỹ và tư vấn tận tâm.',
        'Sản phẩm đúng cấu hình đã chọn, giao hàng cẩn thận.',
        'Hiệu năng tốt, chơi game và lập trình đều ổn định.',
        'Pin dùng lâu, tản nhiệt tốt hơn mong đợi.',
        'Nhân viên hỗ trợ cài đặt nhiệt tình, rất hài lòng.'
    ],
    4: ['Máy tốt, chỉ hơi nặng khi mang đi học.', 'Giao hàng nhanh, pin dùng lâu.', 'Màn hình đẹp nhưng loa hơi nhỏ.'],
    3: ['Máy ổn trong tầm giá, quạt hơi ồn khi tải nặng.'],
    2: ['Giao hàng trễ hai ngày so với dự kiến.'],
    1: ['Máy bị lỗi điểm chết màn hình, đang chờ đổi trả.']
};

const pad = (n: number) => String(n).padStart(2, '0');

// Sinh review có quy luật (không random) để dữ liệu ổn định giữa các lần reload
const generateReviews = (
    product: (typeof products)[number],
    ratings: number[],
    from: Date,
    startId: number,
    startOrderItem: number
): ReviewView[] =>
    ratings.map((rating, i) => {
        const createdAt = new Date(from.getTime() - i * 26 * 3600 * 1000).toISOString();
        const pool = comments[rating];
        return {
            id: startId + i,
            orderItemId: startOrderItem - i,
            ...product,
            customerName: `${lastNames[(i * 3 + product.productId) % lastNames.length]} ${firstNames[(i * 7 + product.productId) % firstNames.length]}`,
            rating,
            comment: pool[i % pool.length],
            // Ẩn vài review thấp sao để tab "Đã ẩn" có dữ liệu
            visible: !(rating <= 2 && i % 2 === 0),
            createdAt,
            updatedAt: createdAt
        };
    });

let reviews: ReviewView[] = [
    {
        id: 1, orderItemId: 218, ...products[0], customerName: 'Nguyễn Minh Tuấn', rating: 5,
        comment: 'Máy chạy mượt, thiết kế đẹp. Màn hình sắc nét và đóng gói rất cẩn thận. Cửa hàng tư vấn cấu hình phù hợp với nhu cầu của mình.',
        visible: true, createdAt: '2026-10-24T08:10:00Z', updatedAt: '2026-10-24T08:10:00Z'
    },
    {
        id: 2, orderItemId: 217, ...products[1], customerName: 'Lê Quốc Huy', rating: 5,
        comment: 'Bàn phím tốt, máy gọn nhẹ.', visible: true, createdAt: '2026-10-24T03:30:00Z', updatedAt: '2026-10-24T03:30:00Z'
    },
    {
        id: 3, orderItemId: 216, ...products[2], customerName: 'Phạm Gia Hân', rating: 4,
        comment: 'Giao hàng nhanh, pin dùng lâu.', visible: true, createdAt: '2026-10-23T09:00:00Z', updatedAt: '2026-10-23T09:00:00Z'
    },
    // ROG Strix G15: tổng 28 review, trung bình 4.9
    ...generateReviews(products[0], [5, 5, 5, 5, 4, 5, 5, 5, 5, 5, 5, 5, 4, 5, 5, 5, 5, 5, 5, 5, 4, 5, 5, 5, 5, 5, 5], new Date('2026-10-20T10:00:00Z'), 100, 200),
    ...generateReviews(products[1], [5, 4, 4, 3, 5, 2, 5, 4], new Date('2026-10-21T07:00:00Z'), 200, 180),
    ...generateReviews(products[2], [5, 5, 4, 1, 5, 4], new Date('2026-10-19T12:00:00Z'), 300, 150),
    ...generateReviews(products[3], [5, 4, 5, 2], new Date('2026-09-28T09:00:00Z'), 400, 120)
];

// Dòng hàng đã giao của khách đang đăng nhập (giả lập)
const myOrderItems: ReviewableOrderItem[] = [
    { orderItemId: 301, orderId: 42, orderCode: 'LS-261010-042', ...products[3], deliveredAt: '2026-10-12T04:00:00Z' },
    { orderItemId: 218, orderId: 41, orderCode: 'LS-261020-041', ...products[0], deliveredAt: '2026-10-22T04:00:00Z' }
];

export const reviewMockStore = {
    getAllReviews: (): ReviewView[] => [...reviews].sort((a, b) => b.createdAt.localeCompare(a.createdAt)),

    getProduct: (productId: number) => products.find(p => p.productId === productId),

    getVisibleByProduct: (productId: number): ReviewView[] =>
        reviewMockStore.getAllReviews().filter(r => r.productId === productId && r.visible),

    // Giống ReviewSummaryResponse: chỉ tính review đang hiển thị, đủ 5 mức sao
    getSummary: (productId: number): ReviewSummary => {
        const visible = reviewMockStore.getVisibleByProduct(productId);
        const ratingCounts = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
        visible.forEach(r => (ratingCounts[r.rating as 1 | 2 | 3 | 4 | 5] += 1));
        const total = visible.length;
        const average = total ? visible.reduce((s, r) => s + r.rating, 0) / total : 0;
        return { productId, averageRating: Math.round(average * 10) / 10, totalReviews: total, ratingCounts };
    },

    setVisible: (reviewId: number, visible: boolean): void => {
        reviews = reviews.map(r => (r.id === reviewId ? { ...r, visible, updatedAt: new Date().toISOString() } : r));
    },

    getOrderItem: (orderItemId: number): ReviewableOrderItem | undefined => myOrderItems.find(i => i.orderItemId === orderItemId),

    getReviewByOrderItem: (orderItemId: number): ReviewView | undefined => reviews.find(r => r.orderItemId === orderItemId),

    // Mỗi dòng hàng chỉ có một review: đã có thì cập nhật, chưa có thì tạo mới
    saveMyReview: (item: ReviewableOrderItem, rating: number, comment: string): ReviewView => {
        const now = new Date().toISOString();
        const existing = reviews.find(r => r.orderItemId === item.orderItemId);
        if (existing) {
            const updated = { ...existing, rating, comment, updatedAt: now };
            reviews = reviews.map(r => (r.id === existing.id ? updated : r));
            return updated;
        }
        const product = products.find(p => p.productId === item.productId)!;
        const created: ReviewView = {
            id: Math.max(0, ...reviews.map(r => r.id)) + 1,
            orderItemId: item.orderItemId,
            ...product,
            customerName: 'Bạn',
            rating,
            comment,
            visible: true,
            createdAt: now,
            updatedAt: now
        };
        reviews = [created, ...reviews];
        return created;
    }
};

export const formatReviewDate = (iso: string): string => {
    const d = new Date(iso);
    return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`;
};
