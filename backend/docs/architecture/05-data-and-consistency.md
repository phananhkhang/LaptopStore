# Dữ liệu và tính nhất quán

## Nguồn chuẩn

Baseline schema của dự án là [`database/LaptopStore_ERD.png`](../../../database/LaptopStore_ERD.png), gồm 25 thực thể. [11 Entity model](11-entity-model.md) ánh xạ tên cột sang field Java; [12 ERD LaptopStore](12-ERD-LaptopStore.md) liệt kê từng thuộc tính, PK/FK/UQ và mọi quan hệ.

Các kiểu dữ liệu, CHECK constraint và index không được thể hiện trên ảnh ERD phải được xem là quyết định triển khai, không được ghi nhầm thành ràng buộc đã có trong ERD.

## Quy ước triển khai đề xuất

Khóa chính có thể dùng Long/BIGINT identity. Tiền VND nên dùng BigDecimal/NUMERIC(19,0), không dùng float/double. Thời gian nên dùng Instant/TIMESTAMPTZ và lưu UTC. Enum nên lưu STRING và kiểm soát tập giá trị ở migration.

Khi tạo migration, nên bổ sung CHECK hợp lý như giá/số lượng không âm và rating trong 1..5, nhưng phải ghi rõ đó là constraint triển khai thêm vì ảnh ERD chỉ mô tả PK/FK/UQ.

## Các bảng và ràng buộc đúng theo ERD

| Bảng hoặc nhóm | Cột/constraint trọng tâm |
|---|---|
| `users`, `roles`, `user_roles` | `users.email` UQ; `roles.name` UQ; PK ghép `user_roles(user_id, role_id)`, cả hai đồng thời là FK |
| `addresses` | `user_id` FK; địa chỉ gồm `ward`, `district`, `city`, `street`, `postal_code` |
| `refresh_tokens` | `user_id` FK; `token_hash` UQ; có `family_id`, `expires_at`, `used_at`, `revoked_at` |
| `brands`, `categories` | `name` UQ; brand có `logo_url`; cả hai có `description`, `active` |
| `products` | `brand_id`, `category_id` là FK; không có `slug` hay `warranty_months` |
| `product_variants` | `product_id` FK; `sku` UQ; có `ram`, `storage`, `color`, `price`, `quantity`, `status` |
| `product_images` | `product_id` FK; có `image_url`, `is_primary`; không có `alt_text`, `sort_order` |
| `product_specifications` | `product_id` chỉ là FK, không phải UQ; có `cpu`, `gpu`, `screen_size`, `screen_resolution`, `refresh_rate`, `battery`, `weight`, `os` |
| `stock_movements` | `variant_id`, `created_by_user_id` là FK; `operation_key` UQ; có before/after, delta, reference và note |
| `carts`, `cart_items` | `carts.user_id` là FK + UQ; cart item có FK tới cart và variant; ERD không đánh UQ cặp cart/variant |
| `orders` | `user_id` FK; `order_code` UQ; `idempotency_key` UQ; snapshot người nhận/địa chỉ/coupon và tổng tiền |
| `order_items` | FK tới order, product, variant; snapshot `product_name`, `variant_name`, `sku`, `unit_price`; không có `line_total` |
| `order_status_history` | FK tới order và user thay đổi; có `from_status`, `to_status`, `changed_at`; không có `reason` |
| `payments` | `order_id` là FK + UQ; có `method`, `amount`, `status`, `expires_at` |
| `payment_transactions` | `payment_id` FK; có provider, provider transaction/reference, amount, status; ERD không đánh UQ provider transaction |
| `coupons`, `coupon_usages` | `coupons.code` UQ; usage có FK tới coupon/user/order; `coupon_usages.order_id` là UQ |
| `suppliers` | Có `name`, `phone`, `email`, `address`, `status`; không có `code` hoặc `active` |
| `purchase_orders`, `purchase_order_items` | PO có FK supplier/user; item có FK PO/variant và `quantity`, `received_quantity`, `unit_cost`; không có unique cặp PO/variant |
| `reviews` | `order_item_id` là FK + UQ; có `rating`, `comment`, `visible`, timestamps |

Foreign key xuyên module có thể tồn tại ở DB dù Java chỉ giữ ID. Index cho FK hay cột truy vấn thường xuyên nên được định nghĩa trong migration, nhưng index không được suy diễn thành UQ.

## Product, Variant và Specification

- `products` đại diện model/dòng laptop.
- `product_variants` đại diện SKU bán thực tế và chứa RAM, storage, color, price, **quantity** và status.
- `product_specifications` chứa thông số kỹ thuật. Theo khóa hiện có, một product có thể có nhiều specification vì `product_id` không được đánh UQ.
- `product_images` chỉ có URL và cờ ảnh chính; thứ tự/alt text chưa thuộc baseline.

## Tồn khả dụng và ledger

Nguồn tồn hiện tại là `product_variants.quantity`. ERD không có bảng `inventory_stocks`. Mỗi lần thay đổi tồn cần:

1. khóa `product_variants` theo ID tăng dần;
2. kiểm tra quantity không âm;
3. cập nhật `product_variants.quantity`;
4. append `stock_movements` với `delta`, `quantity_before`, `quantity_after`, reference, actor và `operation_key` trong cùng transaction.

`stock_movements.operation_key` là UQ toàn cục theo ERD, do đó một nghiệp vụ gồm nhiều variant cần cấp operation key riêng cho từng movement hoặc định nghĩa quy ước key có hậu tố variant. Ledger đã ghi không update/delete; sửa sai bằng movement bù.

Catalog quản lý metadata/giá của variant, còn mọi use case đổi `quantity` phải đi qua inventory service để luôn ghi movement. Khi lưu entity variant, tránh thao tác cập nhật toàn bộ làm ghi đè quantity ngoài ý muốn.

## Snapshot đơn hàng

Khi checkout, sao chép `recipient_name`, `recipient_phone` và địa chỉ đã ghép vào `orders.shipping_address`. Các tổng tiền và `coupon_code_snapshot` cũng được lưu trực tiếp trên order.

`order_items` snapshot `product_name`, `variant_name`, `sku`, `unit_price`, `quantity`; vẫn giữ `product_id` và `variant_id` để truy vết. Thành tiền dòng được tính từ `unit_price * quantity` vì ERD không có `line_total`.

## Payment và trạng thái đơn

Phương thức và trạng thái thanh toán nằm trong `payments`, không nằm trong `orders`. `payments.order_id` là FK + UQ nên mỗi order có tối đa một payment. Các lần tương tác provider được lưu ở `payment_transactions` theo quan hệ 1:N.

Nếu API cần trả `paymentStatus`, service phải đọc/ghép từ `payments.status`; không ánh xạ thành cột không tồn tại trong `orders`.

## Ranh giới transaction và khóa

| Use case | Những phần commit/rollback cùng nhau | Khóa/thứ tự |
|---|---|---|
| Checkout | order/items/history, variant quantity/movement, coupon usage, cart items | cart → variants tăng dần → coupon nếu có |
| Hủy đơn | order/history, coupon usage, variant quantity/movement | order → coupon nếu có → variants tăng dần |
| Nhận phiếu | purchase order/items, variant quantity/movement | purchase order → variants tăng dần |
| Callback | payment/transaction và kiểm tra trạng thái order | payment → order |
| Sửa giỏ | cart/items | cart trước khi sửa items |

Áp dụng thứ tự khóa nhất quán giữa các use case. Không gọi network, gửi email hoặc gọi provider khi đang giữ khóa DB.

## Migration Flyway

Migration là lịch sử tiến hóa schema. Migration khởi tạo phải tạo đủ 25 bảng, PK/FK/UQ đúng ERD và theo thứ tự thỏa mãn FK. Sau khi migration đã merge/chạy ở DB dùng chung, mỗi thay đổi tạo migration version mới; không sửa lịch sử đã áp dụng.

Mọi field/constraint thêm ngoài ERD phải được ghi rõ trong PR và cập nhật đồng thời ERD, tài liệu 11 và tài liệu 12 để ba nguồn không lệch nhau.
