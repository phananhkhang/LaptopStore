import React from 'react';
import { ProductSummaryResponse } from '../../types';
// Import các component con của em (như ProductCard, CategoryFilter) nếu em đã tách ra
// import ProductCard from '../../components/ProductCard';

// 1. MOCK DATA: Giả lập dữ liệu trả về từ API
const MOCK_PRODUCTS: ProductSummaryResponse[] = [
    { id: 1, name: 'ASUS ROG Strix G15', brandName: 'ASUS', categoryName: 'Gaming', status: 'AVAILABLE', thumbnailImage: null, startingPrice: 25000000, active: true },
    { id: 2, name: 'Dell XPS 13', brandName: 'DELL', categoryName: 'Office', status: 'AVAILABLE', thumbnailImage: null, startingPrice: 35000000, active: true },
];

const ProductListPage: React.FC = () => {
    return (
        <div className="max-w-7xl mx-auto p-4 flex flex-col md:flex-row gap-8 mt-6">
            {/* Cột Trái: Filter (Khung giữ chỗ) */}
            <aside className="w-full md:w-1/4 flex-shrink-0 border-r pr-4">
                <h2 className="font-bold text-lg mb-4">Lọc Sản Phẩm</h2>
                <div className="space-y-4">
                    <p className="text-gray-500 italic">... Filter Components ...</p>
                </div>
            </aside>
            
            {/* Cột Phải: Danh sách sản phẩm */}
            <main className="w-full md:w-3/4">
                <div className="flex justify-between items-center mb-6 bg-white p-4 rounded-lg shadow-sm border">
                    <h1 className="text-xl font-bold text-gray-800">Laptop (2)</h1>
                    <select className="border-gray-300 rounded-md py-2 px-4 text-sm border">
                        <option value="createdAt,desc">Mới nhất</option>
                        <option value="price,asc">Giá: Thấp đến cao</option>
                        <option value="price,desc">Giá: Cao đến thấp</option>
                    </select>
                </div>

                {/* Grid Sản Phẩm (Map Mock Data) */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                    {MOCK_PRODUCTS.map(product => (
                        <div key={product.id} className="border rounded-md p-4 shadow-sm hover:shadow-md transition">
                            <div className="h-40 bg-gray-200 rounded-md mb-4 flex items-center justify-center">
                                <span className="text-gray-400">Image</span>
                            </div>
                            <h3 className="font-semibold text-lg line-clamp-2">{product.name}</h3>
                            <p className="text-sm text-gray-500">{product.brandName} | {product.categoryName}</p>
                            <p className="text-red-600 font-bold mt-2">
                                {product.startingPrice?.toLocaleString('vi-VN')} đ
                            </p>
                        </div>
                    ))}
                </div>
            </main>
        </div>
    );
};

export default ProductListPage;