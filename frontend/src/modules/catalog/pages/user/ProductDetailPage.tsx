import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ProductResponse } from '../../types';

// Mock data khớp 100% với DTO ProductResponse anh em mình đã định nghĩa
const MOCK_PRODUCT: ProductResponse = {
    id: 1,
    name: 'Lenovo ThinkPad X1 Carbon Gen 10',
    description: 'Laptop doanh nhân mỏng nhẹ, bền bỉ, hiệu năng cao với bộ vi xử lý Intel thế hệ 12.',
    status: 'ACTIVE',
    brandName: 'Lenovo',
    categoryName: 'Laptop Văn Phòng',
    specification: {
        cpu: 'Intel Core i7-1260P',
        gpu: 'Intel Iris Xe Graphics',
        screenSize: '14.0 inch',
        screenResolution: 'WUXGA (1920x1200) IPS',
        refreshRate: '60Hz',
        battery: '57Wh',
        weight: '1.12 kg',
        os: 'Windows 11 Pro'
    },
    images: [
        { id: 1, imageUrl: 'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800&q=80', isPrimary: true },
        { id: 2, imageUrl: 'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=800&q=80', isPrimary: false },
        { id: 3, imageUrl: 'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&q=80', isPrimary: false },
    ],
    variants: [
        { id: 1, sku: 'TPX1-16-512-BLK', ram: '16GB', storage: '512GB SSD', color: 'Đen Carbon', price: 35000000, quantity: 10, status: 'ACTIVE' },
        { id: 2, sku: 'TPX1-32-1TB-BLK', ram: '32GB', storage: '1TB SSD', color: 'Đen Carbon', price: 42000000, quantity: 5, status: 'ACTIVE' },
    ]
};

const ProductDetailPage: React.FC = () => {
    // Bắt ID từ URL (vd: /products/1)
    const { id } = useParams<{ id: string }>(); 
    
    // Quản lý state cho cấu hình đang chọn và ảnh đang hiển thị
    const [selectedVariant, setSelectedVariant] = useState(MOCK_PRODUCT.variants[0]);
    const [mainImage, setMainImage] = useState(MOCK_PRODUCT.images.find(img => img.isPrimary)?.imageUrl);

    return (
        <div className="max-w-7xl mx-auto p-4 mt-6">
            {/* Breadcrumb điều hướng */}
            <nav className="text-sm mb-6 text-gray-500">
                <Link to="/products" className="hover:text-blue-600 transition-colors">Sản phẩm</Link>
                <span className="mx-2">/</span>
                <span className="hover:text-blue-600 cursor-pointer transition-colors">{MOCK_PRODUCT.brandName}</span>
                <span className="mx-2">/</span>
                <span className="text-gray-800 font-medium">{MOCK_PRODUCT.name}</span>
            </nav>

            <div className="flex flex-col lg:flex-row gap-10">
                {/* Cột trái: Bộ sưu tập hình ảnh */}
                <div className="w-full lg:w-1/2">
                    <div className="bg-white rounded-lg border p-4 mb-4 flex justify-center items-center h-[28rem] shadow-sm">
                        <img src={mainImage} alt={MOCK_PRODUCT.name} className="max-h-full object-contain drop-shadow-md transition-all duration-300" />
                    </div>
                    <div className="flex gap-4 overflow-x-auto pb-2">
                        {MOCK_PRODUCT.images.map(img => (
                            <button 
                                key={img.id} 
                                onClick={() => setMainImage(img.imageUrl)} 
                                className={`border-2 rounded-md p-1 w-24 h-24 flex-shrink-0 transition-all ${mainImage === img.imageUrl ? 'border-blue-600 shadow-md' : 'border-gray-200 hover:border-gray-400'}`}
                            >
                                <img src={img.imageUrl} alt="thumbnail" className="w-full h-full object-cover rounded-sm" />
                            </button>
                        ))}
                    </div>
                </div>

                {/* Cột phải: Thông tin sản phẩm */}
                <div className="w-full lg:w-1/2 flex flex-col">
                    <h1 className="text-3xl font-bold text-gray-900 mb-3">{MOCK_PRODUCT.name}</h1>
                    <p className="text-sm text-gray-500 mb-6 pb-6 border-b">
                        Thương hiệu: <span className="font-semibold text-blue-600 uppercase tracking-wide mr-4">{MOCK_PRODUCT.brandName}</span> 
                        Phân loại: <span className="text-gray-700 mr-4">{MOCK_PRODUCT.categoryName}</span> 
                        SKU: <span className="font-mono text-gray-600">{selectedVariant.sku}</span>
                    </p>
                    
                    <div className="text-4xl font-bold text-red-600 mb-8">
                        {selectedVariant.price.toLocaleString('vi-VN')} ₫
                    </div>

                    {/* Bảng chọn biến thể (Variants) */}
                    <div className="mb-8">
                        <h3 className="font-semibold mb-3 text-gray-800">Tùy chọn cấu hình:</h3>
                        <div className="flex flex-wrap gap-3">
                            {MOCK_PRODUCT.variants.map(variant => (
                                <button 
                                    key={variant.id} 
                                    onClick={() => setSelectedVariant(variant)}
                                    className={`px-5 py-3 border-2 rounded-lg text-sm transition-all ${selectedVariant.id === variant.id ? 'bg-blue-50 border-blue-600 text-blue-800 font-bold shadow-sm' : 'hover:bg-gray-50 border-gray-200 text-gray-700'}`}
                                >
                                    {variant.ram} - {variant.storage}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Nút hành động Mua hàng */}
                    <div className="flex gap-4 mb-10">
                        <button className="flex-1 bg-blue-50 text-blue-700 border border-blue-200 py-3.5 rounded-lg font-bold hover:bg-blue-100 transition-colors shadow-sm">
                            THÊM VÀO GIỎ HÀNG
                        </button>
                        <button className="flex-1 bg-red-600 text-white py-3.5 rounded-lg font-bold hover:bg-red-700 transition-colors shadow-sm">
                            MUA NGAY
                        </button>
                    </div>

                    {/* Bảng thông số kỹ thuật */}
                    <div className="bg-gray-50 p-6 rounded-xl border border-gray-200 mt-auto">
                        <h3 className="font-bold text-lg mb-4 border-b border-gray-300 pb-2 text-gray-800">Thông số kỹ thuật</h3>
                        <ul className="space-y-4 text-sm">
                            <li className="flex"><span className="w-1/3 text-gray-500">Bộ vi xử lý</span><span className="w-2/3 font-medium text-gray-800">{MOCK_PRODUCT.specification.cpu}</span></li>
                            <li className="flex"><span className="w-1/3 text-gray-500">Card đồ họa</span><span className="w-2/3 font-medium text-gray-800">{MOCK_PRODUCT.specification.gpu}</span></li>
                            <li className="flex"><span className="w-1/3 text-gray-500">Màn hình</span><span className="w-2/3 font-medium text-gray-800">{MOCK_PRODUCT.specification.screenSize}, {MOCK_PRODUCT.specification.screenResolution}, {MOCK_PRODUCT.specification.refreshRate}</span></li>
                            <li className="flex"><span className="w-1/3 text-gray-500">Pin & Trọng lượng</span><span className="w-2/3 font-medium text-gray-800">{MOCK_PRODUCT.specification.battery} - {MOCK_PRODUCT.specification.weight}</span></li>
                            <li className="flex"><span className="w-1/3 text-gray-500">Hệ điều hành</span><span className="w-2/3 font-medium text-gray-800">{MOCK_PRODUCT.specification.os}</span></li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProductDetailPage;