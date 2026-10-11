// src/modules/catalog/types/index.ts

export interface ProductImageDto {
    id: number;
    imageUrl: string;
    isPrimary: boolean;
}

export interface ProductSpecificationDto {
    cpu: string;
    gpu: string;
    screenSize: string;
    screenResolution: string;
    refreshRate: string;
    battery: string;
    weight: string;
    os: string;
}

export interface VariantSnapshot {
    id: number;
    sku: string;
    ram: string;
    storage: string;
    color: string;
    price: number;
    quantity: number;
    status: string;
}

export interface ProductSummaryResponse {
    id: number;
    name: string;
    brandName: string;
    categoryName: string;
    status: string;
    thumbnailImage: string | null;
    startingPrice?: number;
    active: boolean;
}

export interface ProductResponse {
    id: number;
    name: string;
    description: string;
    status: string;
    brandName: string;
    categoryName: string;
    specification: ProductSpecificationDto;
    images: ProductImageDto[];
    variants: VariantSnapshot[];
}

// Interface dùng chung để bọc dữ liệu phân trang của Spring Data
export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
}