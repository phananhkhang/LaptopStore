# Module catalog — Danh mục và sản phẩm

**Owner đề xuất:** B theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). **Trạng thái:** skeleton hướng dẫn; các class dưới đây chưa triển khai.

## Trách nhiệm

Sở hữu thông tin laptop, biến thể/SKU, giá bán, ảnh, thông số, brand/category. Catalog **không sở hữu hoặc cập nhật tồn kho**.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | Product, ProductVariant, ProductImage, ProductSpecification, Category, Brand |
| Repository | ProductRepository, ProductVariantRepository, CategoryRepository, BrandRepository |
| Service | ProductService, CategoryService, BrandService, CatalogVariantQueryService |
| Controller | ProductController, CategoryController, BrandController, AdminProductController |
| Request | CreateProductRequest, UpdateProductRequest, CreateVariantRequest |
| Response/contract | ProductResponse, ProductSummaryResponse, VariantSnapshot, CheckoutVariantSnapshot |

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** products, product_variants, product_images, product_specifications, categories, brands.

**Được gọi:** Không gọi module nghiệp vụ khác. Inventory/procurement/cart/order chỉ được dùng query/contract service của catalog; không import repository/entity.

## Bất biến và ràng buộc

- Product là model chung; RAM/SSD/color nằm ở ProductVariant nếu thay đổi theo SKU.
- ProductSpecification baseline 1:1 với Product và dùng typed columns cho CPU/GPU/màn hình/pin/trọng lượng/OS.
- SKU, slug unique; giá BigDecimal không âm; ảnh lưu URL/object key.
- ProductVariant **không có `stockQuantity`**. Tồn nằm tại `inventory.InventoryStock`.
- API public chỉ trả product ACTIVE và variant đang bán; admin có query riêng.
- Checkout dùng `lockAndGetCheckoutVariants(ids)` để chốt giá/trạng thái/SKU snapshot trong transaction; khóa variant theo ID tăng dần.
- Dùng optimistic version cho cập nhật metadata/giá tránh lost update. Không hard-delete variant đã được tham chiếu.

## Bàn giao và kiểm tra

Các ca cần chứng minh: SKU trùng; ẩn sản phẩm inactive; lọc/phân trang; concurrent update giá; ProductSpecification filter đúng; public không thấy giá nhập. Cập nhật API, migration và test module cùng PR.

Chi tiết entity xem [Entity model](../../../../../../../docs/architecture/11-entity-model.md); phụ thuộc xem [allowlist](../../../../../../../docs/architecture/04-dependencies.md).
