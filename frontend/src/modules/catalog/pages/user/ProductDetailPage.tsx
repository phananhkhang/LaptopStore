import React from 'react';
import { useParams } from 'react-router-dom';

// 1. MOCK DATA: Cấu trúc giống hệt ProductResponse DTO từ Backend
const MOCK_PRODUCT_DETAIL = {
    id: 1,
    name: 'ASUS ROG Strix G15',
    brandName: 'ASUS',
    categoryName: 'Gaming',
    description: 'Laptop Gaming ASUS cực mạnh, tản nhiệt tốt, màn hình 144Hz.',
    status: 'AVAILABLE',
    specification: {
        cpu: 'Intel Core i7',
        gpu: 'RTX 3060',
        ram: '16GB',
        storage: '512GB SSD',
        screenSize: '15.6 inch'
    },
    variants: [
        { id: 1, sku: 'ROG-G15-01', price: 25000000, ram: '16GB', storage: '512GB SSD', color: 'Black' },
        { id: 2, sku: 'ROG-G15-02', price: 30000000, ram: '32GB', storage: '1TB SSD', color: 'Black' }
    ]
};

const ProductDetailPage: React.FC = () => {
    // Lấy ID từ URL (ví dụ: /products/1 -> id = "1")
    const { id } = useParams<{ id: string }>();

    return (
        <div className="max-w-7xl mx-auto p-4 mt-6">
            <div className="flex flex-col md:flex-row gap-8">
                {/* Cột Trái: Khung Ảnh Placeholder */}
                <div className="w-full md:w-1/2">
                    <div className="bg-gray-200 aspect-video rounded-lg flex items-center justify-center shadow-inner">
                        <span className="text-gray-500 font-medium">Main Image (ID: {id})</span>
                    </div>
                    <div className="flex gap-2 mt-4">
                        <div className="w-16 h-16 bg-gray-200 rounded"></div>
                        <div className="w-16 h-16 bg-gray-200 rounded"></div>
                        <div className="w-16 h-16 bg-gray-200 rounded"></div>
                    </div>
                </div>

                {/* Cột Phải: Thông tin & Variants */}
                <div className="w-full md:w-1/2">
                    <h1 className="text-2xl font-bold mb-2 text-gray-800">{MOCK_PRODUCT_DETAIL.name}</h1>
                    <p className="text-gray-600 mb-4">
                        Thương hiệu: <span className="font-semibold">{MOCK_PRODUCT_DETAIL.brandName}</span> | Danh mục: <span className="font-semibold">{MOCK_PRODUCT_DETAIL.categoryName}</span>
                    </p>
                    
                    {/* Khu vực chọn Variant */}
                    <div className="mb-6 p-4 bg-gray-50 rounded-lg border">
                        <h3 className="font-semibold mb-3 text-gray-700">Chọn cấu hình:</h3>
                        <div className="flex flex-wrap gap-2">
                            {MOCK_PRODUCT_DETAIL.variants.map((v, index) => (
                                <button 
                                    key={v.id} 
                                    className={`border rounded px-4 py-2 text-sm transition ${index === 0 ? 'border-blue-600 text-blue-600 bg-blue-50 font-medium' : 'border-gray-300 hover:border-blue-400'}`}
                                >
                                    {v.ram} - {v.storage}
                                </button>
                            ))}
                        </div>
                        <p className="text-3xl text-red-600 font-bold mt-6">
                            {MOCK_PRODUCT_DETAIL.variants[0].price.toLocaleString('vi-VN')} đ
                        </p>
                    </div>

                    <button className="bg-blue-600 text-white px-8 py-3 rounded-md hover:bg-blue-700 w-full md:w-auto font-medium transition shadow-sm">
                        Thêm vào giỏ hàng
                    </button>
                </div>
            </div>

            {/* Dưới: Thông số kỹ thuật & Mô tả */}
            <div className="mt-12 border-t pt-8 grid grid-cols-1 md:grid-cols-2 gap-8">
                <div>
                    <h2 className="text-xl font-bold mb-4 text-gray-800">Thông số kỹ thuật</h2>
                    <table className="w-full text-sm text-left text-gray-600">
                        <tbody>
                            <tr className="border-b"><th className="py-2 w-1/3">CPU</th><td>{MOCK_PRODUCT_DETAIL.specification.cpu}</td></tr>
                            <tr className="border-b"><th className="py-2">GPU</th><td>{MOCK_PRODUCT_DETAIL.specification.gpu}</td></tr>
                            <tr className="border-b"><th className="py-2">Màn hình</th><td>{MOCK_PRODUCT_DETAIL.specification.screenSize}</td></tr>
                            <tr><th className="py-2">RAM</th><td>{MOCK_PRODUCT_DETAIL.specification.ram}</td></tr>
                        </tbody>
                    </table>
                </div>
                <div>
                    <h2 className="text-xl font-bold mb-4 text-gray-800">Mô tả sản phẩm</h2>
                    <p className="text-gray-600 leading-relaxed">{MOCK_PRODUCT_DETAIL.description}</p>
                </div>
            </div>
        </div>
    );
};

export default ProductDetailPage;