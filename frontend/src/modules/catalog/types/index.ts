// src/modules/catalog/types/index.ts

export interface Category {
    id: number;
    name: string;
    description?: string;
    active: boolean;
}

export interface Product {
    id: number;
    name: string;
    description?: string;
    status: string;
    // Tạm thời khai báo các trường cơ bản, tuần sau nối API sẽ bổ sung thêm
}