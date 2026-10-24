import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ProductSummaryResponse } from '../../types';
import { productApi } from '../../api/productApi';
import CategoryFilter from '../../components/CategoryFilter';

const ProductListPage: React.FC = () => {
    const [products, setProducts] = useState<ProductSummaryResponse[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [totalElements, setTotalElements] = useState<number>(0);

    // State quản lý bộ lọc
    const [categoryId, setCategoryId] = useState<number | undefined>(undefined);
    const [brandId, setBrandId] = useState<number | undefined>(undefined);
    const [sort, setSort] = useState<string>('createdAt,desc');

    // Gọi API mỗi khi 1 trong 3 tham số (category, brand, sort) thay đổi
    useEffect(() => {
        fetchProducts(categoryId, brandId, sort);
    }, [categoryId, brandId, sort]);

    const fetchProducts = async (catId?: number, brndId?: number, sortParam?: string) => {
        try {
            setLoading(true);
            const params: Record<string, any> = {};
            if (catId) params.categoryId = catId;
            if (brndId) params.brandId = brndId;
            if (sortParam) params.sort = sortParam;

            const response = await productApi.searchProducts(params);
            setProducts(response.content);
            setTotalElements(response.totalElements);
        } catch (error) {
            console.error('Lỗi khi tải danh sách sản phẩm:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleFilterChange = (newCategoryId?: number, newBrandId?: number) => {
        setCategoryId(newCategoryId);
        setBrandId(newBrandId);
    };

    return (
        <div className="max-w-7xl mx-auto p-4 flex flex-col md:flex-row gap-8 mt-6">
            {/* Cột Trái: Component Filter đã được gắn vào */}
            <aside className="w-full md:w-1/4 flex-shrink-0 md:border-r md:pr-6">
                <h2 className="font-bold text-xl mb-6 text-gray-800 border-b pb-2">Lọc Sản Phẩm</h2>
                <CategoryFilter 
                    selectedCategoryId={categoryId} 
                    selectedBrandId={brandId} 
                    onFilterChange={handleFilterChange} 
                />
            </aside>

            {/* Cột Phải: Danh sách sản phẩm */}
            <main className="w-full md:w-3/4">
                <div className="flex justify-between items-center mb-6 bg-gray-50 p-4 rounded-lg border border-gray-100">
                    <h1 className="text-xl font-bold text-gray-800">
                        {categoryId || brandId ? 'Kết quả lọc' : 'Tất cả Laptop'} 
                        <span className="text-blue-600 ml-2">({totalElements})</span>
                    </h1>
                    <select 
                        value={sort}
                        onChange={(e) => setSort(e.target.value)}
                        className="border-gray-300 rounded-md py-2 px-4 text-sm border focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                    >
                        <option value="createdAt,desc">Mới nhất</option>
                        <option value="price,asc">Giá: Thấp đến cao</option>
                        <option value="price,desc">Giá: Cao đến thấp</option>
                    </select>
                </div>

                {loading ? (
                    <div className="flex justify-center items-center h-64">
                        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
                    </div>
                ) : products.length === 0 ? (
                    <div className="text-center py-20 bg-gray-50 rounded-xl border border-dashed border-gray-300">
                        <p className="text-gray-500 text-lg">Không tìm thấy sản phẩm nào phù hợp với bộ lọc.</p>
                        <button 
                            onClick={() => handleFilterChange(undefined, undefined)}
                            className="mt-4 text-blue-600 hover:underline font-medium"
                        >
                            Xóa bộ lọc
                        </button>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
                        {products.map(product => (
                            <div key={product.id} className="border border-gray-200 rounded-xl p-5 hover:shadow-xl hover:border-blue-300 transition-all duration-300 bg-white flex flex-col group">
                                <div className="h-44 bg-white rounded-md mb-4 flex items-center justify-center overflow-hidden">
                                    {product.thumbnailImage ? (
                                        <img src={product.thumbnailImage} alt={product.name} className="h-full object-contain group-hover:scale-105 transition-transform duration-300" />
                                    ) : (
                                        <span className="text-gray-300 text-sm">Chưa có ảnh</span>
                                    )}
                                </div>
                                <h3 className="font-semibold text-gray-800 text-lg line-clamp-2 min-h-[3.5rem] group-hover:text-blue-600 transition-colors">{product.name}</h3>
                                <div className="text-xs font-semibold text-gray-500 mb-3 mt-1 flex gap-2">
                                    <span className="bg-gray-100 px-2 py-1 rounded">{product.brandName}</span>
                                    <span className="bg-gray-100 px-2 py-1 rounded">{product.categoryName}</span>
                                </div>
                                <p className="text-red-600 font-bold mt-auto text-xl">
                                    {product.startingPrice?.toLocaleString('vi-VN')} đ
                                </p>
                                <Link 
                                    to={`/products/${product.id}`} 
                                    className="mt-5 block text-center w-full bg-blue-50 border border-blue-100 text-blue-600 font-medium py-2.5 rounded-lg hover:bg-blue-600 hover:text-white transition-colors"
                                >
                                    Xem cấu hình
                                </Link>
                            </div>
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
};

export default ProductListPage;