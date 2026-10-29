# Module payment — Thanh toán

**Owner đề xuất:** D theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). Chưa gán thành viên thật. **Trạng thái:** skeleton hướng dẫn; các class dưới đây chưa triển khai. Thiết kế chi tiết cần được nhóm chốt khi nhận task.

## Trách nhiệm

Bản ghi thanh toán COD, thanh toán online mô phỏng và tích hợp VNPay ở giai đoạn sau. Xác minh callback và ghi giao dịch.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | Payment, PaymentTransaction |
| Repository | PaymentRepository, PaymentTransactionRepository |
| Service | PaymentService |
| Controller | PaymentController, PaymentCallbackController |
| Request | StartPaymentRequest, RecordCodPaymentRequest |
| Response/contract | PaymentResponse, PaymentSessionResponse |

Đây là danh sách gợi ý theo use case, không bắt buộc tạo đủ class trước khi có nhu cầu.

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** payments, payment_transactions.

**Được gọi:** order.service qua OrderQueryService/OrderPaymentService. Không đọc OrderRepository hoặc ghi entity Order trực tiếp. Không gọi repository/entity module khác. Xem [allowlist và contract](../../../../../../../docs/architecture/04-dependencies.md).

## Bất biến và ràng buộc

- Order được tạo trước; payment khởi tạo bằng request riêng sau checkout. Tạo order không đồng nghĩa đã có giao dịch payment.
- Số tiền lấy từ snapshot order, không tin amount/status do browser gửi.
- Callback phải kiểm tra nguồn/chữ ký theo provider, mã tham chiếu, tiền, currency và kết quả. Redirect browser chỉ dùng hiển thị.
- Deduplicate bằng provider transaction id/event id và khóa payment; nhận callback lặp không tạo giao dịch hay cập nhật order lần hai.
- Ghi payment/transaction và phối hợp trạng thái order qua service trong cùng transaction DB. `orders` không có cột payment projection. Gọi HTTP provider ở ngoài transaction giữ khóa.
- Thanh toán thành công đến sau hủy: lưu chứng cứ và trạng thái cần đối soát trong `payments.status`/`payment_transactions.status`, không đổi order CANCELLED, không rollback mất giao dịch. ERD không có cột `reconciliation_status`; hoàn tiền tự động ngoài MVP.
- Endpoint mô phỏng chỉ mở profile dev/test, có xác thực phù hợp; không dùng nó làm webhook production. COD chỉ STAFF/ADMIN xác nhận thu tiền sau giao hàng.

## Luồng triển khai điển hình

Kiểm tra order/ownership → lập payment → tạo session ngoài khóa DB → nhận callback → khóa payment/order → ghi payment transaction, cập nhật `payments.status` và phối hợp trạng thái order.

## Bàn giao và kiểm tra

Các ca cần chứng minh: sai chữ ký/số tiền; callback lặp; callback trễ sau hủy; timeout provider; retry session; user tự đánh dấu PAID; COD bị ghi hai lần. Cập nhật API, migration và test module cùng PR; ghi kết quả thật. Owner phối hợp người dùng contract khi đổi input/output/lỗi.

## Quy tắc các tầng và liên kết kiến trúc

Quy tắc và ràng buộc chi tiết cho từng tầng (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `integration`) tuân thủ theo [luật tầng](../../../../../../../docs/architecture/02-structure-and-layers.md) và [luồng nghiệp vụ](../../../../../../../docs/architecture/06-business-flows.md).

## Checkout/payment integration

V7 bổ sung metadata request ngoài ERD gốc: orders.checkout_payment_method (COD/VNPAY) và customer_note (500 ký tự); đây là snapshot lựa chọn checkout, không phải trạng thái payment. Idempotency key UNIQUE(user_id, idempotency_key), tối đa 200 ký tự qua API. Checkout khóa cart trước khi kiểm tra key để serialize request đồng thời, gồm method và note trong so sánh payload.

Payment → OrderPaymentService (cạnh đã có trong allowlist): lockPayableOrder khóa đơn, kiểm quyền/method và trả tổng tiền; recordVerifiedPayment xác nhận PENDING_PAYMENT → CONFIRMED, trả false với đơn đã hủy/trả. IPN lưu SUCCESS và payments.status=REQUIRES_RECONCILIATION khi tiền về sau hủy. Trạng thái payment vẫn do payments sở hữu. Hủy đơn VNPAY đã xác nhận cần quy trình đối soát/hoàn tiền, endpoint hiện tại từ chối. POST payments lặp trả payment hiện có; tạo VNPay session lặp tái dùng reference và thời hạn của attempt còn hiệu lực.
