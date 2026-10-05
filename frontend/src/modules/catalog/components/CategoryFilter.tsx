import React from 'react';

const CategoryFilter: React.FC = () => {
    return (
        <div className="bg-white p-5 border rounded-lg shadow-sm">
            <h3 className="font-bold text-lg mb-4 border-b pb-2 text-gray-800">Bộ lọc tìm kiếm</h3>
            
            <div className="mb-6">
                <h4 className="font-semibold mb-3 text-sm text-gray-600 uppercase tracking-wider">Hãng sản xuất</h4>
                <div className="space-y-2 text-sm text-gray-700">
                    <label className="flex items-center cursor-pointer hover:text-blue-600"><input type="checkbox" className="mr-3 rounded border-gray-300 text-blue-600 focus:ring-blue-500" /> Asus</label>
                    <label className="flex items-center cursor-pointer hover:text-blue-600"><input type="checkbox" className="mr-3 rounded border-gray-300 text-blue-600 focus:ring-blue-500" /> Dell</label>
                    <label className="flex items-center cursor-pointer hover:text-blue-600"><input type="checkbox" className="mr-3 rounded border-gray-300 text-blue-600 focus:ring-blue-500" /> Lenovo</label>
                </div>
            </div>

            <div className="mb-4">
                <h4 className="font-semibold mb-3 text-sm text-gray-600 uppercase tracking-wider">Khoảng giá</h4>
                <div className="space-y-2 text-sm text-gray-700">
                    <label className="flex items-center cursor-pointer hover:text-blue-600"><input type="radio" name="price" className="mr-3 text-blue-600 focus:ring-blue-500" /> Dưới 15 triệu</label>
                    <label className="flex items-center cursor-pointer hover:text-blue-600"><input type="radio" name="price" className="mr-3 text-blue-600 focus:ring-blue-500" /> 15 - 25 triệu</label>
                    <label className="flex items-center cursor-pointer hover:text-blue-600"><input type="radio" name="price" className="mr-3 text-blue-600 focus:ring-blue-500" /> Trên 25 triệu</label>
                </div>
            </div>
        </div>
    );
};

export default CategoryFilter;