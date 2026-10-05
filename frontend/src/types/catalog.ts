// src/types/catalog.ts

export interface ProductSearchCriteria {
  keyword?: string;
  brandName?: string;
  categoryName?: string;
  minPrice?: number;
  maxPrice?: number;
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
  // startingPrice?: number; // Mở comment nếu sau này backend trả về
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