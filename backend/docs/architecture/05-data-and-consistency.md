# Dữ liệu và tính nhất quán

## Quy ước chung đề xuất

Khóa chính dùng Long/BIGINT identity cho dữ liệu nghiệp vụ; mã public như orderCode tách khỏi PK. Tiền VND dùng BigDecimal/NUMERIC(19,0), không float/double. Chiết khấu phần trăm tính trung gian đủ precision, kết quả VND làm tròn HALF_UP. Thời gian Instant và TIMESTAMPTZ, lưu UTC; UI chuyển múi giờ. Enum lưu STRING và kiểm soát giá trị bằng migration.

Các bảng thay đổi có created_at/updated_at; ledger/history append-only có created_at, actor và reference phù hợp. Không dùng BaseEntity máy móc để giả rằng bảng nào cũng được update.

## Mô hình dữ liệu tối thiểu

Đây là thiết kế logic; chưa có migration hoặc entity Java tương ứng. Chi tiết field xem [11 Entity model](11-entity-model.md).

| Bảng hoặc nhóm | Trường/constraint quan trọng |
|---|---|
| users, roles, user_roles | unique lower(email), role code unique; PK(user_id, role_id); `user_roles` là join table cho `User.roles`, không phải entity riêng khi không có field nghiệp vụ; password_hash không public |
| addresses | FK user_id; truy vấn luôn kiểm tra chủ sở hữu |
| refresh_tokens | token_hash unique, user_id, family_id, expires_at, used_at, revoked_at; khóa khi rotate |
| brands, categories | slug unique, trạng thái active; parent category nếu có phải tránh vòng |
| products | brand_id/category_id FK, slug unique, trạng thái; model chung, không chứa RAM/SSD/color theo variant |
| product_variants | product_id FK, sku unique, price >= 0, ram_gb/storage_gb > 0, color, status, version; **không chứa stock** |
| product_images | product_id FK; unique(product_id, sort_order) |
| product_specifications | product_id FK unique; các thông số model như CPU/GPU/màn hình/pin/trọng lượng/OS |
| inventory_stocks | variant_id FK unique, quantity >= 0, version; nguồn sự thật về tồn khả dụng |
| stock_movements | variant_id FK, delta, before/after, type, reference_type/id, operation_key, actor; unique(operation_key, variant_id) |
| carts, cart_items | unique user_id ở cart; unique(cart_id, variant_id), quantity > 0 |
| orders | order_code unique, user_id FK, status, payment_method, payment_status projection, snapshot địa chỉ/tổng; unique(user_id, idempotency_key), request_hash |
| order_items | order_id, product_id/variant_id tham chiếu; product_name/SKU/variant_name/unit_price snapshot, quantity > 0, line_total |
| order_status_history | order_id, from/to status, actor, reason, created_at; append-only |
| payments | order_id unique trong MVP, method, status, amount, currency, reconciliation_status |
| payment_transactions | payment_id, provider, provider_transaction_id/reference, type/status/amount; unique provider transaction khi có mã |
| coupons, coupon_usages | code unique; unique(coupon_id, order_id); usage ACTIVE/RELEASED, giới hạn lượt |
| suppliers | mã unique, thông tin liên hệ, active |
| purchase_orders, purchase_order_items | supplier_id FK, code unique, status; quantity > 0, unit_cost >= 0; unique(purchase_order_id, variant_id) |
| reviews | order_item_id unique, user_id/product_id/variant_id; CHECK rating BETWEEN 1 AND 5; moderation status |

Foreign key xuyên module có thể tồn tại ở DB dù Java dùng ID. Index FK cần truy vấn thường xuyên được định nghĩa rõ trong migration. Ràng buộc UNIQUE/CHECK/FK bảo vệ dữ liệu, không thay quyền truy cập và điều kiện nghiệp vụ.

## Product, Variant và Specification

- `Product` đại diện **model chung**, ví dụ “MacBook Air M4 13 inch”.
- `ProductVariant` đại diện **SKU thực sự bán**, ví dụ 16GB/512GB/Midnight. RAM, dung lượng và màu nằm ở variant nếu thay đổi theo SKU.
- `ProductSpecification` là cấu hình kỹ thuật chung của model: CPU, GPU, màn hình, refresh rate, pin, trọng lượng, OS. Baseline dùng một dòng 1:1 với product thay vì key/value tự do để filter/query dễ hơn.
- Nếu sau này có thuộc tính hiếm không đáng tạo cột, có thể thêm `extra_specs JSONB` hoặc bảng extension sau ADR; không đưa vào MVP trước khi cần.

## Tồn khả dụng và ledger

`InventoryStock.quantity` là số lượng **còn được bán**. Baseline chọn **lazy initialization**: variant mới chưa có stock row được hiểu là quantity = 0; lệnh inventory đầu tiên tạo row 0 bằng cơ chế upsert an toàn rồi khóa để cập nhật. Nhận hàng tăng, checkout giảm, hủy hợp lệ tăng lại. DELIVERED không giảm lần nữa.

Inventory sở hữu cả `inventory_stocks` và `stock_movements`. Một lần thay đổi gồm khóa stock row → kiểm tra bất biến → cập nhật quantity/version → append movement trong cùng transaction. Catalog không có API ghi stock và CRUD sản phẩm không được sửa số lượng tồn.

Movement lưu `quantity_before`, `delta`, `quantity_after`, `reference_type`, `reference_id`, `operation_key`; đảm bảo `quantity_after = quantity_before + delta` và không âm. Ledger đã ghi không update/delete; sửa sai bằng movement bù.

## Snapshot đơn hàng

Order không chỉ lưu `address_id`. Khi checkout, sao chép người nhận/điện thoại/địa chỉ và số tiền vào order để lịch sử không thay đổi nếu user sửa profile/address.

OrderItem phải snapshot tối thiểu `product_name`, `sku`, `variant_name`, `unit_price`, `quantity`, `line_total`; vẫn có thể giữ `product_id`/`variant_id` để truy vết nhưng không dùng dữ liệu catalog hiện tại để dựng lại hóa đơn cũ.

## Ranh giới transaction và khóa

| Use case | Những phần commit/rollback cùng nhau | Khóa/thứ tự |
|---|---|---|
| Checkout | order/items/history, stock/ledger, coupon usage, cart items | cart → catalog variants tăng dần → coupon nếu có → inventory stocks tăng dần |
| Hủy đơn | order/history, hoàn coupon, stock/ledger | order → coupon nếu có → inventory stocks tăng dần |
| Nhận phiếu | purchase status, stock/ledger | purchase order → inventory stocks tăng dần |
| Callback | payment/transaction, order payment projection | payment → order; order tuyệt đối không khóa/gọi payment ngược |
| COD thu tiền | payment/transaction, order projection | payment nếu tồn tại → order; ngăn trùng bằng idempotency/constraint |
| Sửa giỏ | cart/items | cart trước khi sửa items |

Áp dụng một thứ tự khóa nhất quán giữa các use case. Không gọi network, gửi email hoặc provider khi đang giữ khóa DB.

Checkout lấy và khóa catalog variants để chốt giá/trạng thái snapshot tại thời điểm đặt hàng, sau đó inventory chỉ chịu trách nhiệm quantity. Vì stock tách khỏi variant nên admin cập nhật metadata/giá không còn có nguy cơ ghi đè số lượng tồn.

## Migration Flyway

Migration là lịch sử tiến hóa schema, không phải file sinh lại từ entity. Giai đoạn bootstrap có thể dùng **một migration khởi tạo cho mỗi module/nhóm nghiệp vụ** theo thứ tự FK. Sau khi migration đã merge/chạy ở DB dùng chung, tuyệt đối không sửa file cũ; mỗi thay đổi schema tạo migration mới với version tiếp theo trên `main`.

Ví dụ baseline:

```text
V1__create_identity_schema.sql
V2__create_catalog_schema.sql
V3__create_cart_schema.sql
V4__create_inventory_schema.sql
V5__create_promotion_schema.sql
V6__create_order_schema.sql
V7__create_payment_schema.sql
V8__create_procurement_schema.sql
V9__create_review_schema.sql
```

Sau này: `V10__add_warranty_months_to_products.sql`, `V11__add_order_status_index.sql`, ... Không chia subfolder migration theo module nếu không cấu hình Flyway locations riêng; timeline phẳng giúp nhóm nhìn thứ tự toàn cục và tránh phụ thuộc FK mơ hồ.
