import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ProductSummaryResponse, ProductSearchCriteria } from '../../types/catalog';
import { catalogApi } from '../../api/catalogApi';

const ProductListPage: React.FC = () => {
  // State lưu trữ dữ liệu
  const [products, setProducts] = useState<ProductSummaryResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [totalElements, setTotalElements] = useState<number>(0);

  // State quản lý bộ lọc (Map chính xác với Backend DTO)
  const [filters, setFilters] = useState<ProductSearchCriteria>({
    brandName: '',
    categoryName: '',
    ram: '',
    storage: '',
    sortPriceDirection: 'asc' // Mặc định giá thấp đến cao
  });

  // Gọi API mỗi khi filters thay đổi
  useEffect(() => {
    fetchProducts(filters);
  }, [filters]);

  const fetchProducts = async (currentFilters: ProductSearchCriteria) => {
    try {
      setLoading(true);
      // Gọi API từ catalogApi đã tạo ở B-2-2
      const response = await catalogApi.searchProducts(currentFilters);
      setProducts(response.content);
      setTotalElements(response.totalElements);
    } catch (error) {
      console.error('Lỗi khi tải danh sách sản phẩm:', error);
    } finally {
      setLoading(false);
    }
  };

  // Hàm xử lý khi check/uncheck vào Sidebar
  const handleFilterChange = (field: keyof ProductSearchCriteria, value: string) => {
    setFilters(prev => ({
      ...prev,
      [field]: prev[field] === value ? '' : value // Nếu click lại cái đang chọn thì bỏ chọn (clear)
    }));
  };

  const handleClearFilters = () => {
    setFilters({ brandName: '', categoryName: '', ram: '', storage: '', sortPriceDirection: 'asc' });
  };

  return (
    <div className="flex flex-col items-center py-7 pb-12 gap-6 w-full max-w-[1280px] mx-auto font-sans">
      
      {/* 1. Breadcrumb & Page Introduction */}
      <div className="w-full flex flex-col gap-1.5 px-4 md:px-0">
        <div className="flex items-center gap-2 text-[12px] mb-2">
          <Link to="/" className="text-ink-lighter font-semibold hover:text-primary">Cửa hàng</Link>
          <span className="text-ink-lighter">/</span>
          <span className="text-ink font-medium">Tất cả laptop</span>
        </div>
        <span className="text-primary text-[11px] font-semibold uppercase tracking-wider">Khám phá công nghệ</span>
        <h1 className="text-ink text-[30px] font-semibold leading-tight">Chọn chiếc laptop dành cho bạn</h1>
        <p className="text-ink-lighter text-[13px]">Từ học tập, làm việc đến những trận game đỉnh cao.</p>
      </div>

      {/* 2. Catalog Layout (Sidebar + Grid) */}
      <div className="w-full flex flex-col md:flex-row gap-6 px-4 md:px-0">
        
        {/* SIDEBAR: Bộ lọc */}
        <aside className="w-full md:w-[232px] shrink-0 bg-white border border-surface-border rounded-[16px] p-5 flex flex-col gap-4 h-fit">
          <h2 className="text-ink text-[16px] font-bold">Bộ lọc tìm kiếm</h2>
          
          {/* Lọc Thương hiệu */}
          <div className="flex flex-col gap-2">
            <h3 className="text-ink text-[12px] font-semibold">Thương hiệu</h3>
            {['ASUS', 'Dell', 'Lenovo', 'HP', 'Apple'].map(brand => (
              <label key={brand} className="flex items-center gap-2.5 cursor-pointer">
                <input 
                  type="checkbox" 
                  checked={filters.brandName === brand}
                  onChange={() => handleFilterChange('brandName', brand)}
                  className="w-4 h-4 rounded border-surface-border text-primary focus:ring-primary accent-primary" 
                />
                <span className="text-ink text-[12px]">{brand}</span>
              </label>
            ))}
          </div>
          <hr className="border-surface-border" />

          {/* Lọc Nhu cầu */}
          <div className="flex flex-col gap-2">
            <h3 className="text-ink text-[12px] font-semibold">Nhu cầu sử dụng</h3>
            {['Gaming', 'Văn phòng', 'Đồ họa'].map(cat => (
              <label key={cat} className="flex items-center gap-2.5 cursor-pointer">
                <input 
                  type="checkbox" 
                  checked={filters.categoryName === cat}
                  onChange={() => handleFilterChange('categoryName', cat)}
                  className="w-4 h-4 rounded border-surface-border text-primary focus:ring-primary accent-primary" 
                />
                <span className="text-ink text-[12px]">{cat}</span>
              </label>
            ))}
          </div>
          <hr className="border-surface-border" />

          {/* Lọc RAM */}
          <div className="flex flex-col gap-2">
            <h3 className="text-ink text-[12px] font-semibold">RAM</h3>
            {['8GB', '16GB', '32GB'].map(ram => (
              <label key={ram} className="flex items-center gap-2.5 cursor-pointer">
                <input 
                  type="checkbox" 
                  checked={filters.ram === ram}
                  onChange={() => handleFilterChange('ram', ram)}
                  className="w-4 h-4 rounded border-surface-border text-primary focus:ring-primary accent-primary" 
                />
                <span className="text-ink text-[12px]">{ram}</span>
              </label>
            ))}
          </div>

          <button 
            onClick={handleClearFilters}
            className="w-full h-[48px] bg-white border border-surface-border text-ink rounded-xl text-[13px] font-semibold hover:bg-gray-50 transition-colors mt-2"
          >
            Xóa bộ lọc
          </button>
        </aside>

        {/* MAIN CONTENT: Lưới sản phẩm */}
        <div className="flex-1 flex flex-col gap-4">
          
          {/* Toolbar */}
          <div className="w-full flex flex-wrap items-center justify-between min-h-[48px] gap-4">
            <div className="flex items-center gap-3">
              <span className="text-ink text-[16px] font-bold">{totalElements} laptop phù hợp</span>
              <span className="bg-orange-50 text-primary px-2 py-1 rounded-lg text-[11px] font-semibold">Đang có hàng</span>
            </div>
            
            <select 
                value={filters.sortPriceDirection}
                onChange={(e) => setFilters(prev => ({...prev, sortPriceDirection: e.target.value}))}
                className="h-[48px] px-3 bg-white border border-surface-border rounded-xl text-ink text-[13px] font-semibold outline-none cursor-pointer focus:border-primary"
            >
                <option value="asc">Giá: Thấp đến cao</option>
                <option value="desc">Giá: Cao đến thấp</option>
            </select>
          </div>

          {/* Product Grid */}
          {loading ? (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-primary"></div>
            </div>
          ) : products.length === 0 ? (
            <div className="text-center py-20 bg-surface-gray rounded-xl border border-surface-border">
                <p className="text-ink-lighter text-[14px]">Không tìm thấy sản phẩm nào phù hợp với bộ lọc.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {products.map((product) => (
                <div key={product.id} className="bg-white border border-surface-border rounded-[16px] p-4 flex flex-col gap-3 hover:shadow-md transition-shadow group">
                  <div className="w-full h-[156px] bg-surface-gray rounded-xl flex justify-center items-center overflow-hidden relative">
                    {product.thumbnailImage ? (
                        <img src={`http://localhost:8081${product.thumbnailImage}`} alt={product.name} className="max-h-[140px] object-contain mix-blend-multiply group-hover:scale-105 transition-transform duration-300" />
                    ) : (
                        <span className="text-ink-lighter text-[11px]">Chưa có ảnh</span>
                    )}
                  </div>
                  <div className="flex flex-col gap-1 mt-1 flex-1">
                    <span className="text-primary text-[10px] font-semibold uppercase">{product.categoryName || 'LAPTOP'}</span>
                    <h3 className="text-ink text-[15px] font-bold line-clamp-2 min-h-[45px]">{product.name}</h3>
                    <p className="text-ink-lighter text-[11px] truncate">{product.brandName}</p>
                    <span className="text-primary text-[18px] font-semibold mt-1">
                      {product.startingPrice ? `${product.startingPrice.toLocaleString('vi-VN')} ₫` : 'Đang cập nhật'}
                    </span>
                  </div>
                  <div className="flex items-center justify-between mt-2 pt-1 border-t border-surface-border/50">
                    <span className="text-ink-lighter text-[11px]">★ 4.9 · Chính hãng</span>
                    <Link to={`/products/${product.id}`} className="text-primary text-[11px] font-semibold hover:underline">
                      Xem chi tiết
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
export default ProductListPage;