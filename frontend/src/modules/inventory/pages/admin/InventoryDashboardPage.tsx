import React, { useEffect, useState } from 'react';
import { inventoryApi } from '../../api/inventoryApi';
import { InventoryStock } from '../../types';

const InventoryDashboardPage: React.FC = () => {
    const [lowStockItems, setLowStockItems] = useState<InventoryStock[]>([]);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        fetchLowStock();
    }, []);

    const fetchLowStock = async () => {
        try {
            setLoading(true);
            // Lấy danh sách biến thể có tồn kho <= 5
            const response = await inventoryApi.getLowStockVariants(5);
            setLowStockItems(response.data);
        } catch (error) {
            console.error("Lỗi khi tải danh sách sắp hết hàng:", error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-6 text-gray-800">Quản lý Kho Hàng (ADMIN/STAFF)</h1>

            <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-xl font-semibold mb-4 text-red-600">Cảnh báo sắp hết hàng (Low Stock)</h2>
                {loading ? (
                    <p className="text-gray-500">Đang tải dữ liệu...</p>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="min-w-full table-auto border-collapse">
                            <thead className="bg-gray-100">
                                <tr>
                                    <th className="px-4 py-2 text-left text-gray-600">Mã SKU</th>
                                    <th className="px-4 py-2 text-left text-gray-600">Sản phẩm</th>
                                    <th className="px-4 py-2 text-left text-gray-600">Biến thể</th>
                                    <th className="px-4 py-2 text-left text-gray-600">Tồn kho</th>
                                    <th className="px-4 py-2 text-left text-gray-600">Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                {lowStockItems.length === 0 ? (
                                    <tr>
                                        <td colSpan={5} className="text-center py-4 text-gray-500">
                                            Kho hàng dồi dào, không có sản phẩm nào sắp hết!
                                        </td>
                                    </tr>
                                ) : (
                                    lowStockItems.map(item => (
                                        <tr key={item.variantId} className="border-b hover:bg-gray-50">
                                            <td className="px-4 py-3 text-sm font-mono">{item.sku}</td>
                                            <td className="px-4 py-3 text-sm font-medium">{item.productName}</td>
                                            <td className="px-4 py-3 text-sm text-gray-500">{item.variantName}</td>
                                            <td className="px-4 py-3 text-sm font-bold text-red-500">{item.quantity}</td>
                                            <td className="px-4 py-3">
                                                <button className="bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700 transition">
                                                    Điều chỉnh
                                                </button>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default InventoryDashboardPage;