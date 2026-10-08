import React from 'react';
import ProductCard from '../../components/ProductCard';
import CategoryFilter from '../../components/CategoryFilter';
import { ProductSummaryResponse } from '../../types';

// Dữ liệu Mock tĩnh chờ tuần sau gọi axios
const MOCK_PRODUCTS: ProductSummaryResponse[] = [
    { id: 1, name: 'Lenovo ThinkPad X1 Carbon Gen 10', brandName: 'Lenovo', categoryName: 'Laptop Văn Phòng', status: 'ACTIVE', thumbnailImage: null },
    { id: 2, name: 'Asus ROG Strix G15 (2022)', brandName: 'Asus', categoryName: 'Laptop Gaming', status: 'ACTIVE', thumbnailImage: null },
    { id: 3, name: 'Dell XPS 13 Plus 9320', brandName: 'Dell', categoryName: 'Ultrabook', status: 'OUT_OF_STOCK', thumbnailImage: null },
    { id: 4, name: 'MacBook Pro 14 M2 Pro', brandName: 'Apple', categoryName: 'Đồ họa - Kỹ thuật', status: 'ACTIVE', thumbnailImage: null },
];

const ProductListPage: React.FC = () => {
    return (
        <div className="max-w-7xl mx-auto p-4 flex flex-col md:flex-row gap-8 mt-6">
            {/* Cột Trái: Filter */}
            <aside className="w-full md:w-1/4 flex-shrink-0">
                <CategoryFilter />
            </aside>
            
            {/* Cột Phải: Grid Sản phẩm & Header */}
            <main className="w-full md:w-3/4">
                <div className="flex flex-col sm:flex-row justify-between items-center mb-6 bg-white p-4 rounded-lg shadow-sm border">
                    <h1 className="text-xl font-bold text-gray-800">Danh sách Laptop <span className="text-gray-500 text-sm font-normal">(Hiển thị 4 kết quả)</span></h1>
                    <select className="mt-4 sm:mt-0 border-gray-300 rounded-md py-2 px-4 text-sm focus:ring-blue-500 focus:border-blue-500 shadow-sm border">
                        <option>Mới nhất</option>
                        <option>Giá: Thấp đến cao</option>
                        <option>Giá: Cao đến thấp</option>
                    </select>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                    {MOCK_PRODUCTS.map(product => (
                        <ProductCard key={product.id} product={product} />
                    ))}
                </div>

                {/* Khung phân trang */}
                <div className="flex justify-center mt-10 space-x-2">
                    <button className="px-4 py-2 border rounded-md text-sm hover:bg-gray-50 text-gray-500 cursor-not-allowed">Trước</button>
                    <button className="px-4 py-2 border rounded-md text-sm bg-blue-600 text-white font-medium">1</button>
                    <button className="px-4 py-2 border rounded-md text-sm hover:bg-gray-50 text-gray-700">2</button>
                    <button className="px-4 py-2 border rounded-md text-sm hover:bg-gray-50 text-gray-700">Sau</button>
                </div>
            </main>
        </div>
    );
};

export default ProductListPage;