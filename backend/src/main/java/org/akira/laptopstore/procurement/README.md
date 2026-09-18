# Module procurement — Nhà cung cấp và nhập hàng

**Owner đề xuất:** B theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). Chưa gán thành viên thật. **Trạng thái:** skeleton hướng dẫn; các class dưới đây chưa triển khai. Thiết kế chi tiết cần được nhóm chốt khi nhận task.

## Trách nhiệm

Supplier, phiếu nhập và dòng nhập. Chỉ khi xác nhận nhận hàng mới cộng tồn. Không quản lý giá bán hay thanh toán công nợ.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | Supplier, PurchaseOrder, PurchaseOrderItem |
| Repository | SupplierRepository, PurchaseOrderRepository, PurchaseOrderItemRepository |
| Service | SupplierService, PurchaseOrderService |
| Controller | SupplierController, PurchaseOrderController |
| Request | CreateSupplierRequest, CreatePurchaseOrderRequest |
| Response/contract | SupplierResponse, PurchaseOrderResponse |

Đây là danh sách gợi ý theo use case, không bắt buộc tạo đủ class trước khi có nhu cầu.

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** suppliers, purchase_orders, purchase_order_items.

**Được gọi:** catalog.service để kiểm tra variant; inventory.service để nhận hàng. Không sửa stock hoặc ledger trực tiếp. Không gọi repository/entity module khác. Xem [allowlist và contract](../../../../../../../docs/architecture/04-dependencies.md).

## Bất biến và ràng buộc

- Phiếu DRAFT chưa ảnh hưởng tồn. MVP nhận toàn bộ phiếu một lần: DRAFT → RECEIVED hoặc CANCELLED.
- Khóa phiếu; kiểm tra trạng thái trước receive; gọi inventory rồi cập nhật RECEIVED trong cùng transaction.
- Dòng nhập quantity > 0, unitCost >= 0; gộp hoặc từ chối SKU trùng để operationKey rõ.
- Phiếu RECEIVED không sửa/xóa/hủy trực tiếp. Sai số xử lý điều chỉnh kho có lý do; trả hàng nhà cung cấp ngoài MVP.
- Supplier bị tham chiếu chỉ ngừng hoạt động; không cascade xóa phiếu cũ. Giá nhập chỉ STAFF/ADMIN có quyền xem.

## Luồng triển khai điển hình

Tạo DRAFT → kiểm tra supplier/variant → nhận hàng với khóa phiếu → inventory tăng tồn → chuyển RECEIVED → commit.

## Bàn giao và kiểm tra

Các ca cần chứng minh: receive hai lần; lỗi một dòng rollback toàn phiếu; xóa supplier đã dùng; nhận phiếu CANCELLED; tồn không đổi khi lưu nháp. Cập nhật API, migration và test module cùng PR; ghi kết quả thật. Owner phối hợp người dùng contract khi đổi input/output/lỗi.

## Quy tắc các tầng và liên kết kiến trúc

Quy tắc và ràng buộc chi tiết cho từng tầng (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) tuân thủ theo [luật tầng](../../../../../../../docs/architecture/02-structure-and-layers.md) và [luồng nghiệp vụ](../../../../../../../docs/architecture/06-business-flows.md).

