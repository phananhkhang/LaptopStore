import React, { useEffect, useState } from 'react';
import { categoryApi } from '../api/categoryApi';
import { brandApi } from '../api/brandApi';
import { Category, Brand } from '../types';

interface CategoryFilterProps {
    selectedCategoryId?: number;
    selectedBrandId?: number;
    onFilterChange: (categoryId?: number, brandId?: number) => void;
}

const CategoryFilter: React.FC<CategoryFilterProps> = ({ selectedCategoryId, selectedBrandId, onFilterChange }) => {
    const [categories, setCategories] = useState<Category[]>([]);
    const [brands, setBrands] = useState<Brand[]>([]);

    useEffect(() => {
        const fetchFilters = async () => {
            try {
                // Gọi song song 2 API để tiết kiệm thời gian chờ
                const [cats, brds] = await Promise.all([
                    categoryApi.getAllCategories(),
                    brandApi.getAllBrands()
                ]);
                // Chỉ hiển thị những mục đang ACTIVE
                setCategories(cats.filter(c => c.active));
                setBrands(brds.filter(b => b.active));
            } catch (error) {
                console.error('Lỗi khi tải danh mục và thương hiệu:', error);
            }
        };
        fetchFilters();
    }, []);

    return (
        <div className="space-y-6">
            {/* Lọc theo danh mục */}
            <div>
                <h3 className="font-semibold text-gray-800 mb-3 uppercase text-sm tracking-wider">Danh mục</h3>
                <ul className="space-y-2 text-sm">
                    <li>
                        <label className="flex items-center space-x-2 cursor-pointer group">
                            <input 
                                type="radio" 
                                name="category"
                                className="form-radio text-blue-600 focus:ring-blue-500"
                                checked={!selectedCategoryId}
                                onChange={() => onFilterChange(undefined, selectedBrandId)}
                            />
                            <span className={!selectedCategoryId ? 'font-bold text-blue-700' : 'text-gray-600 group-hover:text-blue-600'}>
                                Tất cả danh mục
                            </span>
                        </label>
                    </li>
                    {categories.map(cat => (
                        <li key={cat.id}>
                            <label className="flex items-center space-x-2 cursor-pointer group">
                                <input 
                                    type="radio" 
                                    name="category"
                                    className="form-radio text-blue-600 focus:ring-blue-500"
                                    checked={selectedCategoryId === cat.id}
                                    onChange={() => onFilterChange(cat.id, selectedBrandId)}
                                />
                                <span className={selectedCategoryId === cat.id ? 'font-bold text-blue-700' : 'text-gray-600 group-hover:text-blue-600'}>
                                    {cat.name}
                                </span>
                            </label>
                        </li>
                    ))}
                </ul>
            </div>

            {/* Lọc theo thương hiệu */}
            <div className="border-t border-gray-200 pt-5">
                <h3 className="font-semibold text-gray-800 mb-3 uppercase text-sm tracking-wider">Thương hiệu</h3>
                <ul className="space-y-2 text-sm">
                    <li>
                        <label className="flex items-center space-x-2 cursor-pointer group">
                            <input 
                                type="radio" 
                                name="brand"
                                className="form-radio text-blue-600 focus:ring-blue-500"
                                checked={!selectedBrandId}
                                onChange={() => onFilterChange(selectedCategoryId, undefined)}
                            />
                            <span className={!selectedBrandId ? 'font-bold text-blue-700' : 'text-gray-600 group-hover:text-blue-600'}>
                                Tất cả thương hiệu
                            </span>
                        </label>
                    </li>
                    {brands.map(brand => (
                        <li key={brand.id}>
                            <label className="flex items-center space-x-2 cursor-pointer group">
                                <input 
                                    type="radio" 
                                    name="brand"
                                    className="form-radio text-blue-600 focus:ring-blue-500"
                                    checked={selectedBrandId === brand.id}
                                    onChange={() => onFilterChange(selectedCategoryId, brand.id)}
                                />
                                <span className={selectedBrandId === brand.id ? 'font-bold text-blue-700' : 'text-gray-600 group-hover:text-blue-600'}>
                                    {brand.name}
                                </span>
                            </label>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default CategoryFilter;