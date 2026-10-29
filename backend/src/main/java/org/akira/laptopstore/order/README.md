# Module order — Đơn hàng & Checkout

**Owner:** Phan Anh Khang (C theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md)).
**Trạng thái (Tuần 4):** Đã hoàn thiện luồng Checkout service (chọn `addressId` từ sổ địa chỉ hoặc nhập thông tin nhận hàng, validate `paymentMethod`, tính `subtotal`/`discountAmount`/`shippingFee`/`totalAmount`, validate tồn kho và mã giảm giá `couponCode` trước khi tạo đơn, trừ kho atomic, xóa mục đã mua khỏi giỏ hàng và hỗ trợ `Idempotency-Key`) cùng API preview checkout và giao diện Checkout UI.

## Trách nhiệm

Điều phối checkout, snapshot giá/địa chỉ/mã giảm giá, trạng thái đơn và lịch sử. Cung cấp hợp đồng truy vấn/xác thực trạng thái đơn (`OrderQueryService`) cho module `payment` và `review`.

## Thành phần chính

| Thành phần | Class |
|---|---|
| Entity | `Order`, `OrderItem`, `OrderStatusHistory` |
| Enum | `OrderStatus`, `CheckoutPaymentMethod` |
| Repository | `OrderRepository`, `OrderItemRepository`, `OrderStatusHistoryRepository` |
| Service | `OrderService`, `OrderServiceImpl`, `OrderQueryService`, `OrderQueryServiceImpl` |
| Controller | `OrderController` |
| Request DTO | `CreateOrderRequest`, `CheckoutPreviewRequest`, `UpdateOrderStatusRequest` |
| Response DTO | `OrderResponse`, `OrderSummaryResponse`, `OrderPageResponse`, `OrderItemResponse`, `OrderStatusHistoryResponse`, `CheckoutPreviewResponse`, `OrderItemReviewInfo` |

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** `orders`, `order_items`, `order_status_history`.

**Được gọi:** `user.service` (`AddressService`), `cart.service` (`CartService`), `catalog.service` (`CatalogCheckoutService`), `inventory.service` (`InventoryService`), `promotion.service` (`CouponService`). Không gọi `payment` để tránh vòng phụ thuộc. Không gọi repository/entity module khác. Xem [allowlist và contract](../../../../../../../docs/architecture/04-dependencies.md).

## API đã triển khai

- `GET /api/v1/me/orders`: danh sách đơn của người đang đăng nhập.
- `GET /api/v1/me/orders/{orderId}`: chi tiết đơn thuộc người đang đăng nhập; đơn của người khác trả 404.
- `GET /api/v1/staff/orders`: danh sách đơn cho `STAFF`/`ADMIN`.
- `POST /api/v1/me/orders/preview`: xem trước kết quả tính toán checkout (`subtotal`, `discountAmount`, `shippingFee`, `totalAmount`, địa chỉ nhận hàng, phương thức thanh toán, kiểm tra tồn kho và điều kiện coupon) mà không ghi DB.
- `POST /api/v1/me/orders`: tạo đơn hàng từ giỏ hàng hiện tại của user, hỗ trợ header `Idempotency-Key`.

## Bất biến và ràng buộc

- Một transaction bao trùm `order`/`items`/`history`, trừ tồn/ledger (`InventoryService.deductForOrder`), lượt sử dụng coupon (`CouponService.reserveForOrder`) và dọn giỏ (`CartService.clearPurchasedItems`).
- Hỗ trợ chọn địa chỉ qua `addressId` (kiểm tra quyền sở hữu qua `AddressService.getAddressById` và ghép snapshot địa chỉ) hoặc truyền trực tiếp `recipientName`, `recipientPhone`, `shippingAddress`.
- Validate phương thức thanh toán (`COD`, `VNPAY`), kiểm tra tồn kho khả dụng (`availableQuantity`) của từng biến thể và hiệu lực/điều kiện mã giảm giá (`CouponService.evaluateForCheckout`) trước khi lưu `Order`.
- Giá, phí vận chuyển, giảm giá và tổng thanh toán được tính hoàn toàn ở server.
- Hỗ trợ `Idempotency-Key` ràng buộc theo user; cùng key và cùng payload trả lại đơn đã tạo, khác payload trả lỗi.

## Lifecycle và API xử lý đơn

- Trạng thái đóng gói là `PACKING`; V6 chuyển dữ liệu `PACKED` cũ trong orders/history.
- STAFF/ADMIN đọc chi tiết qua GET /api/v1/staff/orders/{orderId}.
- PUT /api/v1/staff/orders/{orderId}/status chỉ cho bước kế tiếp hợp lệ; đơn online chỉ xác nhận sau payment đã xác minh. DELIVERY_FAILED giao lại dưới 3 lần thất bại.
- Hủy và cập nhật trạng thái khóa hàng order. Lặp cùng trạng thái/hủy trả kết quả hiện tại, không ghi lại lịch sử hoặc hoàn kho/coupon.
- Quét hết hạn khóa các đơn PENDING_PAYMENT, xử lý status/history/stock/coupon cùng transaction; lỗi bất kỳ rollback cả đợt và được báo ra scheduler.
- UI flow theo Figma: /cart → /checkout → /orders/:id; /orders; /staff/orders → /staff/orders/:id. Các trạng thái loading, rỗng, lỗi, hết hạn và giao thất bại được xử lý trên từng màn.

## Checkout/payment integration

V7 bổ sung metadata request ngoài ERD gốc: orders.checkout_payment_method (COD/VNPAY) và customer_note (500 ký tự); đây là snapshot lựa chọn checkout, không phải trạng thái payment. Idempotency key UNIQUE(user_id, idempotency_key), tối đa 200 ký tự qua API. Checkout khóa cart trước khi kiểm tra key để serialize request đồng thời, gồm method và note trong so sánh payload.

Payment → OrderPaymentService (cạnh đã có trong allowlist): lockPayableOrder khóa đơn, kiểm quyền/method và trả tổng tiền; recordVerifiedPayment xác nhận PENDING_PAYMENT → CONFIRMED, trả false với đơn đã hủy/trả. IPN lưu SUCCESS và payments.status=REQUIRES_RECONCILIATION khi tiền về sau hủy. Trạng thái payment vẫn do payments sở hữu. Hủy đơn VNPAY đã xác nhận cần quy trình đối soát/hoàn tiền, endpoint hiện tại từ chối. POST payments lặp trả payment hiện có; tạo VNPay session lặp tái dùng reference và thời hạn của attempt còn hiệu lực.
