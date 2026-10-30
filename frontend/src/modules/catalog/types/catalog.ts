// src/modules/catalog/types/catalog.ts

export interface Brand {
  id: number;
  name: string;
  active: boolean;
}

export interface Category {
  id: number;
  name: string;
  active: boolean;
}

export interface ProductSearchCriteria {
  keyword?: string;
  brandName?: string;
  categoryName?: string;
  status?: string;
  ram?: string;
  storage?: string;
  sortPriceDirection?: string; // "asc" hoặc "desc"
  minPrice?: number;
  maxPrice?: number;

  // Các field dùng cho Pageable của Spring Boot
  sort?: string;
  page?: number;
  size?: number;
}

export interface ProductSummaryResponse {
  id: number;
  name: string;
  brandName: string;
  categoryName: string;
  status: string;
  thumbnailImage: string;
  active: boolean;
  startingPrice: number; // Đã map với BigDecimal bên Java
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

export interface ProductImageDto {
  id: number;
  imageUrl: string;
  isPrimary: boolean;
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