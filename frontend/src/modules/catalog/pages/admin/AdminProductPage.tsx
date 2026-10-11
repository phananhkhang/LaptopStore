import React, { useState, useEffect } from 'react';

// Định nghĩa TypeScript interface khớp chặt chẽ với DTO từ Backend
interface Product {
    id: number;
    name: string;
    description: string;
    status: string;
}

const AdminProductPage: React.FC = () => {
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState<boolean>(true);

    // Hàm gọi API lấy danh sách sản phẩm từ Backend
    const fetchProducts = async () => {
        try {
            // URL này sẽ trỏ tới Controller anh em mình vừa viết ở Bước 4
            const response = await fetch('/api/v1/admin/products');
            if (response.ok) {
                const data = await response.json();
                setProducts(data);
            } else {
                console.error('Lỗi khi tải danh sách sản phẩm từ server');
            }
        } catch (error) {
            console.error('Lỗi kết nối API:', error);
        } finally {
            setLoading(false);
        }
    };

    // Tự động lấy dữ liệu ngay khi component vừa mount lên màn hình
    useEffect(() => {
        fetchProducts();
    }, []);

    return (
        <div className="p-6">
            {/* Header và Nút Tạo mới */}
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold text-gray-800">Quản lý Catalog (Admin)</h1>
                <button
                    className="bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700 transition"
                    onClick={() => alert("Form thêm sản phẩm sẽ được mở ở đây!")}
                >
                    + Thêm Sản Phẩm
                </button>
            </div>

            {/* Bảng hiển thị danh sách sản phẩm */}
            <div className="bg-white rounded-lg shadow overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tên Sản Phẩm</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng Thái</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Hành Động</th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {loading ? (
                        <tr>
                            <td colSpan={4} className="px-6 py-4 text-center text-gray-500">Đang tải dữ liệu từ server...</td>
                        </tr>
                    ) : products.length === 0 ? (
                        <tr>
                            <td colSpan={4} className="px-6 py-4 text-center text-gray-500">Chưa có sản phẩm nào trong database.</td>
                        </tr>
                    ) : (
                        products.map(product => (
                            <tr key={product.id}>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{product.id}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{product.name}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm">
                                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                            product.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                                        }`}>
                                            {product.status}
                                        </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                    <button className="text-indigo-600 hover:text-indigo-900 mr-4">Sửa</button>
                                    <button className="text-red-600 hover:text-red-900">
                                        {product.status === 'ACTIVE' ? 'Khóa' : 'Mở khóa'}
                                    </button>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default AdminProductPage;