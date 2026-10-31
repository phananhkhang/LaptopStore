import React, { useEffect, useState } from 'react';
import { catalogApi } from '../../../catalog/api/catalogApi';
import { VariantSnapshot } from '../../../catalog/types/catalog';

// Mở rộng interface Variant để kẹp thêm tên sản phẩm phục vụ hiển thị
interface InventoryItem extends VariantSnapshot {
  productName: string;
  categoryName: string;
}

export const InventoryPage: React.FC = () => {
  const [inventoryItems, setInventoryItems] = useState<InventoryItem[]>([]);
  const [loading, setLoading] = useState(true);

  // State tính toán số liệu thống kê (Metrics)
  const [metrics, setMetrics] = useState({
    totalVariants: 0,
    totalStock: 0,
    lowStock: 0,
    outOfStock: 0
  });

  useEffect(() => {
    fetchInventory();
  }, []);

  const fetchInventory = async () => {
    try {
      setLoading(true);
      // Gọi API lấy sản phẩm (Tạm để size lớn để lấy nhiều data mô phỏng kho)
      const response = await catalogApi.searchProducts({ size: 100 });
      
      let allVariants: InventoryItem[] = [];
      let totalStock = 0;
      let lowStock = 0;
      let outOfStock = 0;


      setInventoryItems(allVariants);
      setMetrics({
        totalVariants: allVariants.length,
        totalStock,
        lowStock,
        outOfStock
      });

    } catch (error) {
      console.error("Lỗi lấy dữ liệu kho hàng", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-[1200px] mx-auto flex flex-col gap-6 font-sans">
      
      {/* Page Title */}
      <div className="flex justify-between items-end">
        <div className="flex flex-col gap-1">
          <span className="text-primary text-[11px] font-semibold uppercase tracking-wider">Vận hành / Kho hàng</span>
          <h1 className="text-ink text-[30px] font-bold leading-tight">Tồn kho luôn trong tầm mắt.</h1>
          <p className="text-ink-lighter text-[13px]">Theo dõi tồn thực tế theo từng SKU.</p>
        </div>
        <button className="bg-primary text-white px-5 py-3 rounded-xl text-[13px] font-semibold hover:bg-primary-dark transition-colors">
          Tạo phiếu nhập
        </button>
      </div>

      {/* Metrics */}
      <div className="grid grid-cols-4 gap-4">
        <div className="bg-white border border-surface-border rounded-[16px] p-5 flex flex-col gap-1">
          <span className="text-ink-lighter text-[12px] font-medium">Tổng biến thể (SKU)</span>
          <span className="text-ink text-[28px] font-bold">{metrics.totalVariants}</span>
          <span className="text-[#16835B] text-[11px] font-semibold mt-1">Đang kinh doanh</span>
        </div>
        <div className="bg-white border border-surface-border rounded-[16px] p-5 flex flex-col gap-1">
          <span className="text-ink-lighter text-[12px] font-medium">Số lượng tồn</span>
          <span className="text-ink text-[28px] font-bold">{metrics.totalStock.toLocaleString('vi-VN')}</span>
          <span className="text-[#16835B] text-[11px] font-semibold mt-1">Máy sẵn sàng giao</span>
        </div>
        <div className="bg-white border border-surface-border rounded-[16px] p-5 flex flex-col gap-1">
          <span className="text-ink-lighter text-[12px] font-medium">Sắp hết hàng</span>
          <span className="text-ink text-[28px] font-bold">{metrics.lowStock}</span>
          <span className="text-[#C74615] text-[11px] font-semibold mt-1">Tồn kho ≤ 5 máy</span>
        </div>
        <div className="bg-white border border-surface-border rounded-[16px] p-5 flex flex-col gap-1">
          <span className="text-ink-lighter text-[12px] font-medium">Hết hàng</span>
          <span className="text-ink text-[28px] font-bold">{metrics.outOfStock}</span>
          <span className="text-[#BE3E3E] text-[11px] font-semibold mt-1">Cần nhập gấp</span>
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="flex items-center gap-3 w-full">
         <div className="flex-1 bg-white border border-surface-border rounded-xl h-[48px] px-3 flex items-center gap-2">
            <svg className="w-4 h-4 text-ink-lighter" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
            <input type="text" placeholder="Tìm tên laptop, mã SKU..." className="w-full bg-transparent border-none outline-none text-[12px] text-ink" />
         </div>
         <button className="bg-white border border-surface-border px-5 h-[48px] rounded-xl text-ink text-[13px] font-semibold hover:bg-surface-gray">
            Tất cả danh mục
         </button>
         <button className="bg-white border border-surface-border px-5 h-[48px] rounded-xl text-ink text-[13px] font-semibold hover:bg-surface-gray">
            Tồn kho thấp
         </button>
      </div>

      {/* Inventory Table */}
      <div className="bg-white border border-surface-border rounded-[16px] overflow-hidden flex flex-col">
        <div className="bg-surface-gray h-[48px] flex items-center px-4 border-b border-surface-border">
          <div className="w-[170px] text-ink-lighter text-[11px] font-bold tracking-wider">SKU</div>
          <div className="w-[380px] text-ink-lighter text-[11px] font-bold tracking-wider">SẢN PHẨM / CẤU HÌNH</div>
          <div className="w-[140px] text-ink-lighter text-[11px] font-bold tracking-wider">TỒN KHO</div>
          <div className="w-[200px] text-ink-lighter text-[11px] font-bold tracking-wider">TRẠNG THÁI</div>
          <div className="w-[214px] text-ink-lighter text-[11px] font-bold tracking-wider text-right">THAO TÁC</div>
        </div>

        {loading ? (
           <div className="p-8 text-center text-ink-lighter text-sm">Đang tải dữ liệu kho...</div>
        ) : inventoryItems.length === 0 ? (
           <div className="p-8 text-center text-ink-lighter text-sm bg-[#FFF2E9]">
             <span className="text-[#C74615] font-semibold">Không có kết quả phù hợp</span>
             <br/>Thử bỏ bớt điều kiện lọc hoặc chọn Tất cả.
           </div>
        ) : (
          <div className="flex flex-col divide-y divide-surface-border">
            {inventoryItems.map((item) => (
              <div key={item.id} className="h-[70px] flex items-center px-4 hover:bg-surface-gray/30 transition-colors">
                <div className="w-[170px] text-ink text-[12px] font-semibold">{item.sku}</div>
                
                <div className="w-[380px] flex flex-col justify-center">
                   <span className="text-ink text-[12px] truncate pr-4">
                     {item.productName} · {item.ram} / {item.storage}
                   </span>
                </div>
                
                <div className="w-[140px] text-ink text-[12px] font-medium">{item.quantity}</div>
                
                <div className="w-[200px]">
                   {item.quantity === 0 ? (
                     <span className="bg-[#FFF0F0] text-[#BE3E3E] px-2.5 py-1.5 rounded-lg text-[11px] font-semibold">Hết hàng</span>
                   ) : item.quantity <= 5 ? (
                     <span className="bg-[#FFF2E9] text-[#C74615] px-2.5 py-1.5 rounded-lg text-[11px] font-semibold">Sắp hết hàng</span>
                   ) : (
                     <span className="bg-[#EAF7F0] text-[#16835B] px-2.5 py-1.5 rounded-lg text-[11px] font-semibold">Đang kinh doanh</span>
                   )}
                </div>

                <div className="w-[214px] text-right text-primary text-[12px] font-semibold cursor-pointer hover:underline pr-2">
                   Điều chỉnh &rarr;
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

    </div>
  );
};
export default InventoryPage; // Cần export default để trùng với cấu trúc code cũ của em