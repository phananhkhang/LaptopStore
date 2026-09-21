# Module inventory — Tồn kho và lịch sử biến động

**Owner đề xuất:** B theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). **Trạng thái:** skeleton hướng dẫn; các class dưới đây chưa triển khai.

## Trách nhiệm

Inventory là **nguồn sự thật duy nhất về số lượng tồn**. Module sở hữu cả stock hiện tại và ledger biến động; catalog không được ghi stock.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | InventoryStock, StockMovement |
| Repository | InventoryStockRepository, StockMovementRepository |
| Service | InventoryService, InventoryQueryService |
| Controller | InventoryController/AdminInventoryController |
| Request | AdjustStockRequest |
| Response/contract | InventoryStockResponse, StockMovementResponse, StockChangeResult |

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** inventory_stocks, stock_movements.

`inventory_stocks.variant_id` có FK DB sang `product_variants.id`, nhưng Java chỉ giữ `Long variantId`; không `@ManyToOne ProductVariant`.

**Được gọi:** catalog query contract nếu cần xác minh variant/SKU. Không import `ProductVariantRepository`, không gọi order/procurement và không dùng entity module khác.

## Bất biến và ràng buộc

- `InventoryStock.quantity >= 0`; một variant có tối đa một stock row trong mô hình một kho MVP. Missing row = 0; lệnh đầu tiên lazy-create bằng upsert an toàn rồi khóa row.
- Mọi biến động sau khởi tạo đi qua InventoryService; checkout giảm, cancel restore tăng, purchase receipt tăng.
- Cập nhật stock và append StockMovement trong cùng transaction; ledger lỗi phải rollback stock.
- Mỗi lệnh có `operationKey`; unique `(operation_key, variant_id)` để retry không cộng/trừ hai lần.
- Lệnh nhiều dòng gộp theo variant rồi khóa `InventoryStock` theo variant ID tăng dần.
- SALE delta âm; CANCEL_RESTORE/PURCHASE_RECEIPT delta dương; ADJUSTMENT bắt buộc reason + actor.
- Movement append-only; lưu before/delta/after/reference. DELIVERED không giảm stock lần nữa.

## Luồng triển khai điển hình

Nhận lệnh nghiệp vụ → deduplicate → khóa stock rows → kiểm tra đủ quantity → cập nhật quantity/version → append movement → commit cùng transaction caller.

## Bàn giao và kiểm tra

Các ca cần chứng minh: hai request mua SKU còn 1; lệnh lặp; rollback khi ledger lỗi; tồn âm; khóa nhiều variant theo thứ tự; adjustment thiếu lý do; receive purchase lặp không cộng hai lần.

Chi tiết entity xem [Entity model](../../../../../../../docs/architecture/11-entity-model.md); phụ thuộc xem [allowlist](../../../../../../../docs/architecture/04-dependencies.md).
