# Module inventory — Tồn kho và lịch sử biến động

**Owner đề xuất:** B theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). **Trạng thái:** skeleton hướng dẫn; các class dưới đây chưa triển khai.

## Trách nhiệm

Inventory là **use-case owner duy nhất của thay đổi số lượng tồn**. Theo ERD, stock hiện tại nằm ở `product_variants.quantity`; lịch sử biến động nằm ở `stock_movements`.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | StockMovement |
| Repository | StockMovementRepository |
| Service | InventoryService, InventoryQueryService |
| Controller | InventoryController/AdminInventoryController |
| Request | AdjustStockRequest |
| Response/contract | InventoryQuantityResponse, StockMovementResponse, StockChangeResult |

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** quyền ghi nghiệp vụ lên `product_variants.quantity` và bảng `stock_movements`.

`stock_movements.variant_id` có FK DB sang `product_variants.id`, nhưng Java chỉ giữ `Long variantId`; không `@ManyToOne ProductVariant`.

**Được gọi:** catalog contract để khóa variant, đọc/cập nhật `quantity` và xác minh SKU. Không import `ProductVariantRepository`, không gọi order/procurement và không dùng entity module khác.

## Bất biến và ràng buộc

- `product_variants.quantity` không được âm; ERD không có bảng `inventory_stocks`.
- Mọi biến động sau khởi tạo đi qua InventoryService; checkout giảm, cancel restore tăng, purchase receipt tăng.
- Cập nhật stock và append StockMovement trong cùng transaction; ledger lỗi phải rollback stock.
- Mỗi StockMovement có `operationKey` UQ toàn cục để retry không cộng/trừ hai lần. Lệnh nhiều dòng phải tạo key riêng ổn định cho từng movement, ví dụ ghép key nghiệp vụ với variant ID.
- Lệnh nhiều dòng gộp theo variant rồi khóa ProductVariant theo ID tăng dần.
- SALE delta âm; CANCEL_RESTORE/PURCHASE_RECEIPT delta dương; ADJUSTMENT bắt buộc reason + actor.
- Movement append-only; lưu before/delta/after/reference. DELIVERED không giảm stock lần nữa.

## Luồng triển khai điển hình

Nhận lệnh nghiệp vụ → deduplicate → khóa ProductVariant → kiểm tra đủ quantity → cập nhật `product_variants.quantity` → append movement → commit cùng transaction caller.

## Bàn giao và kiểm tra

Các ca cần chứng minh: hai request mua SKU còn 1; lệnh lặp; rollback khi ledger lỗi; tồn âm; khóa nhiều variant theo thứ tự; adjustment thiếu lý do; receive purchase lặp không cộng hai lần.

Chi tiết entity xem [Entity model](../../../../../../../docs/architecture/11-entity-model.md); phụ thuộc xem [allowlist](../../../../../../../docs/architecture/04-dependencies.md).
