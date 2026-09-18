# Dữ liệu và tính nhất quán

## Quy ước chung đề xuất

Khóa chính dùng Long/BIGINT identity cho dữ liệu nghiệp vụ; mã public như orderCode tách khỏi PK. Tiền VND dùng BigDecimal/NUMERIC(19,0), không float/double. Chiết khấu phần trăm tính trung gian đủ precision, kết quả VND làm tròn HALF_UP. Thời gian Instant và TIMESTAMPTZ, lưu UTC; UI chuyển múi giờ. Enum lưu STRING và kiểm soát giá trị bằng migration.

Các bảng thay đổi có created_at/updated_at; ledger/history append-only có created_at, actor và reference phù hợp. Không dùng BaseEntity máy móc để giả rằng bảng nào cũng được update.

## Mô hình dữ liệu tối thiểu

Đây là thiết kế logic; chưa có migration hoặc entity tương ứng.

| Bảng hoặc nhóm | Trường/constraint quan trọng |
|---|---|
| users, roles, user_roles | unique lower(email), role code unique; unique(user_id, role_id); password_hash không public |
| addresses | FK user_id; truy vấn luôn kiểm tra chủ sở hữu |
| refresh_tokens | token_hash unique, user_id, expires_at, used_at, revoked_at; khóa khi rotate |
| brands, categories | slug unique, trạng thái active; parent category nếu có phải tránh vòng |
| products | brand_id/category_id FK, slug unique, trạng thái; index phục vụ lọc |
| product_variants | product_id FK, sku unique, price >= 0, stock_quantity >= 0, version |
| product_images, product_specifications | product_id FK; unique(product_id, sort_order) cho ảnh; tên/thứ tự thông số rõ |
| carts, cart_items | unique user_id ở cart; unique(cart_id, variant_id), quantity > 0 |
| orders | order_code unique, user_id FK, status, payment_method, payment_status projection, snapshot địa chỉ/tổng; unique(user_id, idempotency_key), request_hash |
| order_items | order_id và variant_id FK; tên/SKU/giá snapshot, quantity > 0 |
| order_status_history | order_id, from/to status, actor, reason, created_at; append-only |
| payments | order_id FK, method, status, amount, currency, attempt_key; unique(order_id, attempt_key), reconciliation_status |
| payment_transactions | payment_id FK, provider, provider_transaction_id, result; unique(provider, provider_transaction_id) cho giao dịch có mã |
| coupons, coupon_usages | code unique; unique(coupon_id, order_id); usage ACTIVE/RELEASED, giới hạn lượt |
| stock_movements | variant_id FK, delta, before/after, type, reference_type/id, operation_key, actor; unique(operation_key, variant_id) |
| suppliers | mã unique, thông tin liên hệ, active |
| purchase_orders, purchase_order_items | supplier_id FK, code unique, status; quantity > 0, unit_cost >= 0; unique(purchase_order_id, variant_id) |
| reviews | order_item_id FK unique, user_id/variant_id; CHECK rating BETWEEN 1 AND 5; moderation status |

Foreign key xuyên module có thể tồn tại ở DB dù Java dùng ID. Index FK cần truy vấn thường xuyên được định nghĩa rõ trong migration. Ràng buộc UNIQUE/CHECK/FK bảo vệ dữ liệu, không thay quyền truy cập và điều kiện nghiệp vụ. Tham khảo [PostgreSQL constraints](https://www.postgresql.org/docs/17/ddl-constraints.html).

## Tồn khả dụng và ledger

`ProductVariant.stockQuantity` là số lượng **còn được bán**, không phải báo cáo vật lý on-hand khi hàng đang giữ cho đơn. Tạo variant với 0; nhận hàng tăng, checkout giảm, hủy hợp lệ tăng lại. DELIVERED không giảm lần nữa. Chưa có reservedQuantity hoặc bảng Stock riêng.

Inventory là nơi quyết định nghiệp vụ biến động; catalog giữ entity/repository và cung cấp thao tác ghi thấp. Một lần thay đổi gồm cập nhật variant và append movement trong cùng transaction. Mọi delta phải có reference, actor/reason khi thích hợp và operationKey ổn định; không tạo key mới khi retry cùng một nghiệp vụ.

## Ranh giới transaction và khóa

| Use case | Những phần commit/rollback cùng nhau | Khóa/thứ tự |
|---|---|---|
| Checkout | order/items/history, stock/ledger, coupon usage, cart items | cart → coupon nếu có → variants tăng dần; order mới chưa bị cạnh tranh |
| Hủy đơn | order/history, hoàn coupon, stock/ledger | order → coupon nếu có → variants tăng dần |
| Nhận phiếu | purchase status, stock/ledger | purchase order → variants tăng dần |
| Callback | payment/transaction, order payment projection | payment → order; order tuyệt đối không khóa/gọi payment ngược |
| COD thu tiền | payment/transaction, order projection | payment nếu tồn tại → order; ngăn trùng bằng idempotency/constraint |
| Sửa giỏ | cart/items | cart trước khi sửa items |

Áp dụng một thứ tự khóa nhất quán giữa các use case. Không gọi network, gửi email hoặc chờ người dùng khi đang giữ khóa. Khóa hàng và việc khóa sai thứ tự có thể gây deadlock; xem [PostgreSQL explicit locking](https://www.postgresql.org/docs/17/explicit-locking.html).

Stock contract đọc và ghi entity có khóa pessimistic và version; cập nhật metadata variant dùng version để báo xung đột. Checkout truyền expectedVersion từ snapshot theo từng variant; sau khi khóa, catalog so version và trạng thái bán. Nếu khác, baseline trả 409 `PRODUCT_CHANGED` và rollback toàn bộ để khách kiểm tra lại; không tự chốt snapshot cũ. Một thiết kế tự tính lại giá/coupon dưới khóa cần cập nhật contract riêng trước khi dùng.

Runtime business exception phải rollback các thay đổi liên quan; checked exception cần cấu hình rollback rõ nếu dùng. Không nuốt exception. Transaction thất bại do constraint/lock phải rollback xong mới retry có giới hạn; không tiếp tục query trong transaction đã hỏng.

## Idempotency

- Checkout: key scoped theo user, lưu request hash (payload checkout chuẩn hóa, không hash giỏ đã bị dọn), tra order cũ trước đọc giỏ; cùng key/payload trả order cũ. Request tranh chấp unique: rollback request thua rồi đọc lại trong transaction mới.
- Nhập hàng/hủy: khóa bản ghi chủ; trạng thái kết thúc là chốt chống thực hiện lại, cộng thêm operationKey ledger và trạng thái usage.
- Payment: cùng provider transaction/event không ghi lần hai. Không đồng nhất “callback nhận lại” với “khách trả tiền hai lần”; hai mã giao dịch thành công khác nhau cần giữ dấu vết và đánh dấu đối soát, không tăng doanh thu hợp lệ hai lần.
- Idempotency không thay kiểm tra actor/ownership; người khác không được lấy response chỉ bằng biết key.

## Migration và lịch sử

File trong `src/main/resources/db/migration` theo `VyyyyMMddHHmmss__module_description.sql`, timestamp UTC duy nhất trong nhóm. FK migration chạy sau bảng được tham chiếu. Không sửa file đã được dùng chung; thêm migration tiến về phía trước. Seed role là dữ liệu hệ thống có kiểm soát; dữ liệu demo dùng test/dev riêng, không seed user admin với mật khẩu thật.

Giữ `ddl-auto=validate`; chưa dùng `baseline-on-migrate` để che schema lệch. DB có bảng thủ công cần kế hoạch baseline riêng trước triển khai. Không xóa volume để “sửa” lỗi migration trên dữ liệu cần giữ.
