import React from 'react';
import { Link } from 'react-router-dom';

const SupplierListPage: React.FC = () => {
    return (
        <div className="p-6 max-w-7xl mx-auto">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold text-gray-800">Quản lý Nhà cung cấp</h1>
                <button className="bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700 transition">
                    + Thêm mới
                </button>
            </div>
            <div className="bg-white p-8 rounded-lg shadow-sm border text-center">
                <p className="text-gray-500 italic">... Bảng danh sách Nhà cung cấp (Tích hợp supplierApi) sẽ được xây dựng tại đây ...</p>
            </div>
        </div>
    );
};

export default SupplierListPage;