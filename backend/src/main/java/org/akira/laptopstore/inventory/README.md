# Module inventory — Tồn kho và lịch sử biến động

**Owner đề xuất:** B theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). Chưa gán thành viên thật. **Trạng thái:** skeleton hướng dẫn; các class dưới đây chưa triển khai. Thiết kế chi tiết cần được nhóm chốt khi nhận task.

## Trách nhiệm

Quyết định tăng/giảm tồn và ghi ledger. StockMovement thuộc inventory; stockQuantity vẫn nằm tại ProductVariant của catalog.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | StockMovement |
| Repository | StockMovementRepository |
| Service | InventoryService |
| Controller | InventoryController |
| Request | AdjustStockRequest |
| Response/contract | StockMovementResponse, StockChangeResult |

Đây là danh sách gợi ý theo use case, không bắt buộc tạo đủ class trước khi có nhu cầu.

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** stock_movements; quyền nghiệp vụ đối với product_variants.stock_quantity qua catalog service.

**Được gọi:** catalog.service.VariantStockService; không import ProductVariantRepository và không gọi order/procurement. Không gọi repository/entity module khác. Xem [allowlist và contract](../../../../../../../docs/architecture/04-dependencies.md).

## Bất biến và ràng buộc

- Mọi biến động sau khởi tạo variant đi qua InventoryService. Bản đầu dùng stockQuantity là tồn khả dụng, trừ ngay lúc checkout.
- Cập nhật số lượng qua catalog contract và append StockMovement trong cùng transaction; lỗi ledger phải rollback tồn.
- Mỗi lệnh có operationKey do use case tạo; unique(operation_key, variant_id). Lệnh nhiều dòng gộp theo variant rồi khóa id tăng dần. Với checkout, truyền expectedVersion từ snapshot xuống catalog stock contract để phát hiện giá/trạng thái thay đổi.
- SALE có delta âm; CANCEL/PURCHASE_RECEIPT có delta dương; ADJUSTMENT có lý do và actor. Trước/sau phải khớp delta, tồn sau >= 0.
- Ledger đã ghi không sửa/xóa; sai số được bù bằng movement mới. DELIVERED không trừ tồn lần nữa.
- Catalog metadata không biết nguyên nhân nghiệp vụ thay đổi tồn; inventory xác thực reference/reason trước khi gọi stock contract.

## Luồng triển khai điển hình

Nhận lệnh nghiệp vụ → deduplicate → khóa variant → đổi stock qua catalog → append movement → commit cùng transaction gọi vào.

## Bàn giao và kiểm tra

Các ca cần chứng minh: tranh chấp tồn cuối; lệnh lặp; rollback khi ledger lỗi; tồn âm; khóa nhiều variant theo thứ tự; điều chỉnh thiếu lý do. Cập nhật API, migration và test module cùng PR; ghi kết quả thật. Owner phối hợp người dùng contract khi đổi input/output/lỗi.

## Quy tắc các tầng và liên kết kiến trúc

Quy tắc và ràng buộc chi tiết cho từng tầng (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) tuân thủ theo [luật tầng](../../../../../../../docs/architecture/02-structure-and-layers.md) và [luồng nghiệp vụ](../../../../../../../docs/architecture/06-business-flows.md).

