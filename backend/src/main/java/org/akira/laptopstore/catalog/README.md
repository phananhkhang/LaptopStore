# Module catalog — Danh mục và sản phẩm

**Owner đề xuất:** B theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). Chưa gán thành viên thật. **Trạng thái:** skeleton hướng dẫn; các class dưới đây chưa triển khai. Thiết kế chi tiết cần được nhóm chốt khi nhận task.

## Trách nhiệm

Sở hữu thông tin laptop, biến thể, SKU, giá bán, ảnh, thông số, brand/category. Cung cấp truy vấn sản phẩm và thao tác ghi tồn mức thấp cho inventory.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | Product, ProductVariant, ProductImage, ProductSpecification, Category, Brand |
| Repository | ProductRepository, ProductVariantRepository, CategoryRepository, BrandRepository |
| Service | ProductService, CategoryService, BrandService, VariantStockService |
| Controller | ProductController, CategoryController, BrandController, AdminProductController |
| Request | CreateProductRequest, UpdateProductRequest, CreateVariantRequest |
| Response/contract | ProductResponse, ProductSummaryResponse, VariantSnapshot |

Đây là danh sách gợi ý theo use case, không bắt buộc tạo đủ class trước khi có nhu cầu.

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** products, product_variants, product_images, product_specifications, categories, brands.

**Được gọi:** Không gọi module nghiệp vụ khác. VariantStockService là contract nội bộ chỉ inventory được gọi để ghi tồn. Không gọi repository/entity module khác. Xem [allowlist và contract](../../../../../../../docs/architecture/04-dependencies.md).

## Bất biến và ràng buộc

- API public chỉ trả product ACTIVE và variant đang bán; truy vấn admin có contract riêng.
- SKU, slug có unique constraint; giá BigDecimal không âm; trường ảnh là URL/object key, không nhét binary vào bảng product.
- ProductVariant.stockQuantity biểu diễn tồn khả dụng. Tạo variant với tồn 0; nhập/điều chỉnh tồn đi qua inventory. DTO CRUD sản phẩm không cho sửa stockQuantity.
- VariantStockService khóa variant theo id tăng dần, kiểm tra không âm và cập nhật trong transaction của inventory; không tự ghi StockMovement. Checkout truyền expectedVersion và yêu cầu còn bán; snapshot thay đổi trả PRODUCT_CHANGED để rollback.
- Dùng version để phát hiện cập nhật cũ trên variant, tránh sửa metadata ghi đè tồn. Không hard-delete variant đã được tham chiếu.

## Luồng triển khai điển hình

Controller lọc tham số → ProductService kiểm tra quyền/trạng thái → query có phân trang → map DTO.

## Bàn giao và kiểm tra

Các ca cần chứng minh: SKU trùng; ẩn sản phẩm inactive; lọc/phân trang; cập nhật metadata không ghi đè tồn; public không thấy giá nhập. Cập nhật API, migration và test module cùng PR; ghi kết quả thật. Owner phối hợp người dùng contract khi đổi input/output/lỗi.

## Quy tắc các tầng và liên kết kiến trúc

Quy tắc và ràng buộc chi tiết cho từng tầng (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) tuân thủ theo [luật tầng](../../../../../../../docs/architecture/02-structure-and-layers.md) và [luồng nghiệp vụ](../../../../../../../docs/architecture/06-business-flows.md).

