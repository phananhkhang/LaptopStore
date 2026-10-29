// Khớp với các DTO trong backend: org.akira.laptopstore.review.dto
export interface Review {
    id: number;
    orderItemId: number;
    productId: number;
    variantId: number;
    productName: string;
    variantName: string;
    rating: number;
    comment: string | null;
    visible: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface ReviewSummary {
    productId: number;
    averageRating: number;
    totalReviews: number;
    ratingCounts: Record<1 | 2 | 3 | 4 | 5, number>;
}

export interface CreateReviewRequest {
    orderItemId: number;
    rating: number;
    comment?: string;
}

export interface UpdateReviewRequest {
    rating?: number;
    comment?: string;
}

// Page<T> của Spring Data
export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

// ===== Kiểu dữ liệu chỉ dùng cho UI =====

// Backend chưa trả tên khách và SKU trong ReviewResponse
export interface ReviewView extends Review {
    customerName: string;
    sku: string;
}

// Dòng hàng đã giao mà khách có thể đánh giá
export interface ReviewableOrderItem {
    orderItemId: number;
    orderId: number;
    orderCode: string;
    productId: number;
    variantId: number;
    productName: string;
    variantName: string;
    deliveredAt: string;
}
