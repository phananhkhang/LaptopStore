# Bảng Tiêu Chí Nghiệm Thu Tuần 3 - Module Catalog, Inventory, Procurement
**Người thực hiện:** Kiệt

## 1. Xử lý nợ kỹ thuật (Technical Debt) & Schema
- ✅ Khắc phục lỗi schema lệch entity: Thêm cột `active` vào `products` và `product_variants` qua file migration mới (V3), đảm bảo không sửa file đã merge.
- ✅ Đảm bảo Spring Boot khởi động an toàn với `ddl-auto=validate` trên DB sạch.
- ✅ Khởi tạo dữ liệu mẫu (Seed Data) qua file migration V4 để phục vụ quá trình test API.

## 2. Chuẩn hóa luồng DTO & Tránh rò rỉ dữ liệu
- ✅ Không trả về Entity trực tiếp ở các API List/Detail, loại bỏ hoàn toàn lỗi đệ quy JSON (Infinite Recursion).
- ✅ Bổ sung và cấu hình MapStruct tự động tính toán các trường `startingPrice`, `thumbnailImage`, `active` cho Search DTO.

## 3. Tái cấu trúc Logic Tồn kho (Inventory Ledger)
- ✅ Xóa quyền chỉnh sửa `quantity` trực tiếp từ phía Catalog (Dọn dẹp Request DTO).
- ✅ Xây dựng luồng `adjustStock`: Mọi thay đổi tồn kho bắt buộc gọi qua `InventoryService`.
- ✅ Ghi nhận đầy đủ lịch sử biến động kho vào bảng `StockMovement` (Ledger).

## 4. API & Tính năng Tuần 3 (Backend)
- ✅ Xây dựng thành công API lấy danh sách hình ảnh và thông số kỹ thuật (Spec/Images).
- ✅ Hoàn thiện Product Detail API: Trả về 1 DTO tổng hợp duy nhất (Product, Variants, Specs, Images).
- ✅ Khắc phục thành công lỗi `MultipleBagFetchException` bằng kỹ thuật tách truy vấn `@EntityGraph`.
- ✅ Xử lý Search/Filter: Nâng cấp Criteria API lọc theo Keyword, Brand, Category.
- ✅ Lọc theo RAM/Storage: Hỗ trợ tìm kiếm mờ (Fuzzy Match), tự động loại bỏ khoảng trắng và dấu gạch ngang.
- ✅ Xử lý Sort: Giải quyết lỗi `DISTINCT + ORDER BY` của PostgreSQL bằng `EXISTS` Subquery. Xử lý thành công việc Sort theo giá động nằm ở bảng con Variant, chặn Pageable override.

## 5. Cấu trúc Frontend (React)
- ✅ Khởi tạo Types và cấu trúc thư mục chuẩn cho module Catalog.
- ✅ Dựng thành công Skeleton Component cho `ProductListPage` (Danh sách) với Mock Data.
- ✅ Dựng thành công Skeleton Component cho `ProductDetailPage` (Chi tiết) với Mock Data chuẩn DTO.
- ✅ Kết nối thành công `CatalogRoutes` vào `AppRouter`, đảm bảo luồng điều hướng trang hoạt động tốt.

**Đánh giá tổng quan:**
- Các tiêu chí Tuần 3 của Role Kiệt đã được đáp ứng 100%.
- Không có tiêu chí nào bị bỏ sót (❌) hay chưa hoàn thiện (⚠️).