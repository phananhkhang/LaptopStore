import React, { useEffect, useState } from 'react';
import { inventoryApi } from '../../api/inventoryApi';
import { StockMovement } from '../../types';

const StockMovementHistoryPage: React.FC = () => {
    const [movements, setMovements] = useState<StockMovement[]>([]);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        fetchHistory();
    }, []);

    const fetchHistory = async () => {
        try {
            setLoading(true);
            // Lấy trang đầu tiên, 20 record
            const response = await inventoryApi.getAllMovements(0, 20);
            setMovements(response.data.content);
        } catch (error) {
            console.error("Lỗi khi tải lịch sử kho:", error);
        } finally {
            setLoading(false);
        }
    };

    const getTypeColor = (type: string) => {
        if (type === 'SALE' || type === 'OUT') return 'text-red-600 bg-red-100';
        if (type === 'IN' || type === 'PURCHASE_RECEIPT' || type === 'CANCEL_RESTORE') return 'text-green-600 bg-green-100';
        return 'text-gray-600 bg-gray-100';
    };

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-6 text-gray-800">Lịch sử Biến động Kho</h1>

            <div className="bg-white shadow rounded-lg p-6">
                {loading ? (
                    <p className="text-gray-500">Đang tải dữ liệu...</p>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="min-w-full table-auto border-collapse">
                            <thead className="bg-gray-100">
                                <tr>
                                    <th className="px-4 py-2 text-left text-gray-600">Thời gian</th>
                                    <th className="px-4 py-2 text-left text-gray-600">Sản phẩm</th>
                                    <th className="px-4 py-2 text-left text-gray-600">Loại</th>
                                    <th className="px-4 py-2 text-left text-gray-600">Thay đổi</th>
                                    <th className="px-4 py-2 text-left text-gray-600">Tồn sau</th>
                                    <th className="px-4 py-2 text-left text-gray-600">Ghi chú</th>
                                </tr>
                            </thead>
                            <tbody>
                                {movements.map(m => (
                                    <tr key={m.id} className="border-b hover:bg-gray-50">
                                        <td className="px-4 py-3 text-sm text-gray-500">
                                            {new Date(m.createdAt).toLocaleString('vi-VN')}
                                        </td>
                                        <td className="px-4 py-3 text-sm">
                                            <div className="font-medium">{m.productName}</div>
                                            <div className="text-xs text-gray-400 font-mono">{m.sku}</div>
                                        </td>
                                        <td className="px-4 py-3 text-sm">
                                            <span className={`px-2 py-1 rounded text-xs font-semibold ${getTypeColor(m.type)}`}>
                                                {m.type}
                                            </span>
                                        </td>
                                        <td className={`px-4 py-3 text-sm font-bold ${m.delta > 0 ? 'text-green-600' : 'text-red-600'}`}>
                                            {m.delta > 0 ? `+${m.delta}` : m.delta}
                                        </td>
                                        <td className="px-4 py-3 text-sm font-medium">{m.quantityAfter}</td>
                                        <td className="px-4 py-3 text-sm text-gray-500">{m.note || '-'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default StockMovementHistoryPage;