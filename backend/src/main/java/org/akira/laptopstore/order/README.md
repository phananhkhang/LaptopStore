# Module order — Đơn hàng

**Owner đề xuất:** C theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). Chưa gán thành viên thật. **Trạng thái:** đã có query foundation cho lịch sử và chi tiết đơn; checkout và workflow ghi đơn còn chờ contract liên module.

## Trách nhiệm

Điều phối checkout, snapshot giá/địa chỉ, trạng thái đơn, hủy và lịch sử. Sở hữu điểm nhận kết quả thanh toán do payment xác minh.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | Order, OrderItem, OrderStatusHistory |
| Repository | OrderRepository, OrderItemRepository, OrderStatusHistoryRepository |
| Service | OrderService, OrderPaymentService, OrderQueryService |
| Controller | OrderController, StaffOrderController |
| Request | PlaceOrderRequest, ChangeOrderStatusRequest |
| Response/contract | OrderResponse, OrderSummaryResponse, OrderPaymentSnapshot, PurchasedItemView |

Đây là danh sách gợi ý theo use case, không bắt buộc tạo đủ class trước khi có nhu cầu.

DTO request/response dùng class với getter theo chuẩn JavaBean (`getOrderCode()`, `getItems()`, ...), constructor rỗng và constructor đầy đủ. Các tên trường JSON và constraint validation được giữ nguyên.

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** orders, order_items, order_status_history.

**Được gọi:** user.service, cart.service, catalog.service, inventory.service, promotion.service. Không gọi payment để tránh vòng phụ thuộc. Không gọi repository/entity module khác. Xem [allowlist và contract](../../../../../../../docs/architecture/04-dependencies.md).

## API hiện có

- `GET /api/v1/me/orders`: danh sách đơn của người đang đăng nhập.
- `GET /api/v1/me/orders/{orderId}`: chi tiết đơn thuộc người đang đăng nhập; đơn của người khác trả 404.
- `GET /api/v1/staff/orders`: danh sách đơn cho STAFF/ADMIN.

Hai endpoint danh sách nhận `page`, `size`, `sort` của Spring Data và trả `content`, `totalElements`, `totalPages`, `number`, `size`.

Mapper chỉ chuyển dữ liệu do module order sở hữu. `lineTotal` được tính từ snapshot `unitPrice × quantity`; tổng tiền order vẫn đọc từ snapshot đã lưu.

Chưa mở endpoint tạo đơn, hủy đơn hay đổi trạng thái. Các thao tác này phải được triển khai cùng contract cart/catalog/inventory/promotion và transaction checkout ở luồng bên dưới.

## Bất biến và ràng buộc

- Một transaction bao trùm order/items/history, trừ tồn/ledger, lượt coupon và dọn giỏ. Không gọi HTTP cổng thanh toán trong transaction này.
- Giá, phí, giảm giá và tổng tính ở server. Order snapshot người nhận/điện thoại/địa chỉ; OrderItem snapshot productName, SKU, variantName, unitPrice và lineTotal để lịch sử không đổi khi dữ liệu gốc sửa.
- Checkout hỗ trợ Idempotency-Key ràng buộc user; cùng key và payload trả đơn cũ, khác payload trả 409.
- Khóa order trước khi chuyển trạng thái/hủy. Chỉ hoàn tồn/coupon một lần khi chuyển thành CANCELLED.
- Order.paymentStatus chỉ là bản phản chiếu cho workflow; chỉ OrderPaymentService cập nhật theo kết quả đã xác minh từ payment. Không có HTTP endpoint tự đánh dấu đã trả tiền.
- Bản đầu không hủy tự động đơn online đã PAID; cần quy trình hoàn tiền riêng. Không hồi sinh đơn CANCELLED khi nhận thanh toán trễ.

## Luồng triển khai điển hình

Khóa giỏ → kiểm tra địa chỉ → catalog khóa variants và chốt snapshot giá/SKU/trạng thái → khóa/kiểm tra coupon → tạo order để có ID → inventory khóa InventoryStock và trừ tồn/ghi ledger → hoàn tất items/history/coupon → dọn giỏ → commit. Order tạo sớm vẫn rollback nếu bước sau thất bại; không trả đơn ra ngoài trước commit.

## Bàn giao và kiểm tra

Các ca cần chứng minh: rollback giữa checkout; hai người mua SKU cuối; request lặp; hủy lặp; chuyển trạng thái sai; khách đọc đơn người khác; callback thanh toán chạy đua với hủy. Cập nhật API, migration và test module cùng PR; ghi kết quả thật. Owner phối hợp người dùng contract khi đổi input/output/lỗi.

## Quy tắc các tầng và liên kết kiến trúc

Quy tắc và ràng buộc chi tiết cho từng tầng (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) tuân thủ theo [luật tầng](../../../../../../../docs/architecture/02-structure-and-layers.md) và [luồng nghiệp vụ](../../../../../../../docs/architecture/06-business-flows.md).

