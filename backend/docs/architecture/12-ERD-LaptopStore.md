# Mô hình dữ liệu quan hệ (ERD) — LaptopStore

> Nguồn chuẩn: [`database/LaptopStore_ERD.png`](../../../database/LaptopStore_ERD.png).
> Thuộc tính và quan hệ trong tài liệu này được đọc theo **PK / FK / UQ hiển thị trong ERD**. Không suy diễn thêm cột hoặc unique constraint chỉ từ yêu cầu nghiệp vụ.

**Rà soát:** 2026-10-01. Danh sách 25 bảng và toàn bộ cột dưới đây khớp ảnh ERD. [11 Entity model](11-entity-model.md) bổ sung tên field/kiểu Java và các khác biệt của source; [13 Phân quyền và chức năng](13-roles-and-functions.md) nối các bảng với use case và quyền thiết kế.

## 1. Quy ước

- **PK (Primary Key):** khóa chính, định danh duy nhất một bản ghi.
- **FK (Foreign Key):** khóa ngoại, tham chiếu sang entity khác.
- **UQ (Unique):** giá trị/cặp giá trị không được trùng.
- **PK/FK:** field vừa thuộc khóa chính vừa là khóa ngoại, thường dùng trong bảng liên kết.
- **1 : N:** một bản ghi phía cha có thể liên kết với nhiều bản ghi phía con.
- **1 : 0..1:** một bản ghi phía cha có tối đa một bản ghi phía con; thường được enforce bằng `FK + UQ`.
- **N : N:** quan hệ nhiều-nhiều, phải triển khai qua bảng trung gian trong mô hình quan hệ.

`1:N` mô tả bội số tối đa phía con; không có nghĩa cha bắt buộc có ít nhất một con. `FK + UQ` chỉ bảo đảm cha có tối đa một bản ghi con; để khẳng định mỗi con bắt buộc có cha còn cần `NOT NULL`. Ảnh không công bố nullability, kiểu SQL, default, CHECK, index hay quy tắc `ON DELETE`, nên các chi tiết đó phải lấy từ migration được áp dụng.

## 2. Tổng quan các nhóm thực thể

- **Identity & Access:** `users`, `roles`, `user_roles`, `addresses`, `refresh_tokens`
- **Catalog:** `brands`, `categories`, `products`, `product_variants`, `product_images`, `product_specifications`
- **Cart:** `carts`, `cart_items`
- **Ordering:** `orders`, `order_items`, `order_status_history`
- **Payment:** `payments`, `payment_transactions`
- **Promotion:** `coupons`, `coupon_usages`
- **Inventory:** `stock_movements`
- **Procurement:** `suppliers`, `purchase_orders`, `purchase_order_items`
- **Review:** `reviews`

Tổng cộng: **25 bảng, 192 cột, 30 khóa ngoại và 14 ràng buộc UQ ngoài PK**. Có 24 PK đơn `id` và một PK ghép `user_roles(user_id, role_id)`. Đếm mỗi cột FK của bảng nối riêng; quan hệ N:N users–roles được tạo từ hai FK đó.

ERD không có `inventory_stocks`, `audit_logs`, bảng giữ chỗ tồn kho hoặc bảng quyền chức năng riêng. Một class Java rỗng hay một chức năng trong tài liệu 13 không làm phát sinh bảng trong baseline.

## 3. Identity & Access

### `users`

Tài khoản người dùng và thông tin xác thực/trạng thái tài khoản.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| UQ | `email` | Email đăng nhập. |
| — | `password_hash` | Mật khẩu đã băm. |
| — | `full_name` | Họ tên. |
| — | `phone` | Số điện thoại. |
| — | `token_version` | Phiên bản token để hỗ trợ vô hiệu hóa access token. |
| — | `status` | Trạng thái nghiệp vụ của bản ghi. |
| — | `created_at` | Thời điểm tạo. |
| — | `updated_at` | Thời điểm cập nhật gần nhất. |

### `roles`

Danh mục vai trò trong hệ thống, ví dụ USER, STAFF, ADMIN.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| UQ | `name` | Mã vai trò dùng để phân quyền; dữ liệu seed hiện là ROLE_USER, ROLE_STAFF, ROLE_ADMIN. |

### `user_roles`

Bảng trung gian triển khai quan hệ nhiều-nhiều giữa users và roles.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK/FK | `user_id` | Tham chiếu người dùng. |
| PK/FK | `role_id` | Tham chiếu vai trò. |

### `addresses`

Các địa chỉ giao hàng đã lưu của người dùng.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `user_id` | Tham chiếu người dùng. |
| — | `recipient_name` | Tên người nhận. |
| — | `phone` | Số điện thoại. |
| — | `ward` | Phường/xã. |
| — | `district` | Quận/huyện. |
| — | `city` | Tỉnh/thành phố. |
| — | `street` | Địa chỉ đường/số nhà. |
| — | `postal_code` | Mã bưu chính. |
| — | `is_default` | Đánh dấu địa chỉ mặc định. |
| — | `created_at` | Thời điểm tạo. |
| — | `updated_at` | Thời điểm cập nhật gần nhất. |

### `refresh_tokens`

Lưu refresh token phục vụ đăng nhập dài hạn, rotation và thu hồi phiên.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `user_id` | Tham chiếu người dùng. |
| UQ | `token_hash` | Giá trị refresh token đã băm. |
| — | `family_id` | Nhóm refresh token cùng một chuỗi rotation. |
| — | `expires_at` | Thời điểm hết hạn. |
| — | `used_at` | Thời điểm token đã được sử dụng. |
| — | `revoked_at` | Thời điểm token bị thu hồi. |
| — | `created_at` | Thời điểm tạo. |

## 4. Catalog

### `brands`

Danh mục thương hiệu laptop.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| UQ | `name` | Tên hiển thị. |
| — | `logo_url` | Đường dẫn logo. |
| — | `description` | Mô tả. |
| — | `active` | Trạng thái kích hoạt. |

### `categories`

Danh mục phân loại sản phẩm.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| UQ | `name` | Tên hiển thị. |
| — | `description` | Mô tả. |
| — | `active` | Trạng thái kích hoạt. |

### `products`

Thông tin sản phẩm ở cấp model/dòng laptop.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `brand_id` | Tham chiếu thương hiệu. |
| FK | `category_id` | Tham chiếu danh mục. |
| — | `name` | Tên hiển thị. |
| — | `description` | Mô tả. |
| — | `status` | Trạng thái nghiệp vụ của bản ghi. |
| — | `created_at` | Thời điểm tạo. |
| — | `updated_at` | Thời điểm cập nhật gần nhất. |

### `product_variants`

Biến thể bán hàng của sản phẩm, phân biệt theo RAM, storage, color, SKU và giá.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `product_id` | Tham chiếu sản phẩm. |
| — | `ram` | Dung lượng RAM của biến thể. |
| — | `storage` | Dung lượng lưu trữ của biến thể. |
| — | `color` | Màu sắc biến thể. |
| — | `price` | Giá bán. |
| — | `quantity` | Tồn kho hiện tại của SKU; mọi thay đổi thuộc use case inventory và đi cùng stock movement. |
| UQ | `sku` | Mã SKU. |
| — | `status` | Trạng thái nghiệp vụ của bản ghi. |
| — | `created_at` | Thời điểm tạo. |
| — | `updated_at` | Thời điểm cập nhật gần nhất. |

### `product_images`

Các hình ảnh thuộc một sản phẩm.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `product_id` | Tham chiếu sản phẩm. |
| — | `image_url` | Đường dẫn hình ảnh. |
| — | `is_primary` | Đánh dấu ảnh chính. |

### `product_specifications`

Thông số kỹ thuật chi tiết của sản phẩm.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `product_id` | Tham chiếu sản phẩm. |
| — | `cpu` | Thông số CPU. |
| — | `gpu` | Thông số GPU. |
| — | `screen_size` | Kích thước màn hình. |
| — | `screen_resolution` | Độ phân giải màn hình. |
| — | `refresh_rate` | Tần số quét. |
| — | `battery` | Thông tin pin. |
| — | `weight` | Khối lượng. |
| — | `os` | Hệ điều hành. |

## 5. Cart

### `carts`

Giỏ hàng thuộc về một người dùng.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK, UQ | `user_id` | Tham chiếu người dùng. |
| — | `created_at` | Thời điểm tạo. |
| — | `updated_at` | Thời điểm cập nhật gần nhất. |

### `cart_items`

Các biến thể sản phẩm đang nằm trong giỏ hàng.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `cart_id` | Tham chiếu giỏ hàng. |
| FK | `variant_id` | Tham chiếu biến thể sản phẩm. |
| — | `quantity` | Số lượng người dùng chọn trong giỏ; không phải tồn được giữ chỗ. |
| — | `created_at` | Thời điểm tạo. |
| — | `updated_at` | Thời điểm cập nhật gần nhất. |

## 6. Ordering

### `orders`

Đơn hàng và các giá trị tổng tiền/snapshot thông tin giao hàng tại thời điểm đặt.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `user_id` | Tham chiếu người dùng. |
| UQ | `order_code` | Mã đơn hàng. |
| — | `status` | Trạng thái nghiệp vụ của bản ghi. |
| — | `subtotal` | Tạm tính trước giảm giá và phí giao hàng. |
| — | `discount_amount` | Số tiền giảm thực tế được ghi nhận. |
| — | `shipping_fee` | Phí giao hàng. |
| — | `total_amount` | Tổng tiền đơn hàng. |
| — | `recipient_name` | Tên người nhận. |
| — | `recipient_phone` | Số điện thoại người nhận tại thời điểm đặt hàng. |
| — | `shipping_address` | Snapshot địa chỉ giao hàng tại thời điểm đặt hàng. |
| — | `coupon_code_snapshot` | Snapshot mã coupon đã áp dụng. |
| UQ | `idempotency_key` | Khóa chống tạo đơn lặp khi request bị retry. |
| — | `created_at` | Thời điểm tạo. |
| — | `updated_at` | Thời điểm cập nhật gần nhất. |

### `order_items`

Các dòng hàng thuộc đơn hàng, đồng thời lưu snapshot tên, SKU và giá khi mua.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `order_id` | Tham chiếu đơn hàng. |
| FK | `product_id` | Tham chiếu sản phẩm. |
| FK | `variant_id` | Tham chiếu biến thể sản phẩm. |
| — | `product_name` | Snapshot tên sản phẩm tại thời điểm mua. |
| — | `variant_name` | Snapshot tên/mô tả biến thể tại thời điểm mua. |
| — | `sku` | Snapshot SKU tại thời điểm mua; không có UQ trên cột này. |
| — | `unit_price` | Đơn giá tại thời điểm mua. |
| — | `quantity` | Số lượng mua của dòng đơn hàng. |

### `order_status_history`

Lịch sử chuyển trạng thái của đơn hàng.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `order_id` | Tham chiếu đơn hàng. |
| — | `from_status` | Trạng thái trước khi chuyển. |
| — | `to_status` | Trạng thái sau khi chuyển. |
| — | `changed_at` | Thời điểm chuyển trạng thái. |
| FK | `changed_by_user_id` | Người thực hiện thay đổi trạng thái. |

## 7. Payment

### `payments`

Trạng thái thanh toán logic của một đơn hàng.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK, UQ | `order_id` | Tham chiếu đơn hàng. |
| — | `method` | Phương thức thanh toán. |
| — | `amount` | Số tiền. |
| — | `status` | Trạng thái nghiệp vụ của bản ghi. |
| — | `created_at` | Thời điểm tạo. |
| — | `updated_at` | Thời điểm cập nhật gần nhất. |
| — | `expires_at` | Thời điểm hết hạn. |

### `payment_transactions`

Các giao dịch/lần tương tác với nhà cung cấp thanh toán thuộc một payment.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `payment_id` | Tham chiếu payment. |
| — | `provider` | Nhà cung cấp/cổng thanh toán. |
| — | `provider_transaction_id` | Mã giao dịch phía nhà cung cấp. |
| — | `provider_reference` | Mã tham chiếu phía nhà cung cấp. |
| — | `amount` | Số tiền. |
| — | `status` | Trạng thái nghiệp vụ của bản ghi. |
| — | `created_at` | Thời điểm tạo. |

## 8. Promotion

### `coupons`

Định nghĩa mã giảm giá, điều kiện áp dụng, giới hạn sử dụng và thời gian hiệu lực.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| UQ | `code` | Mã coupon. |
| — | `discount_type` | Loại giảm giá. |
| — | `discount_value` | Giá trị giảm. |
| — | `max_discount` | Mức giảm tối đa. |
| — | `min_order_amount` | Giá trị đơn hàng tối thiểu. |
| — | `usage_limit` | Giới hạn số lần dùng toàn hệ thống. |
| — | `usage_per_user` | Giới hạn số lần dùng trên mỗi user. |
| — | `starts_at` | Thời điểm bắt đầu hiệu lực. |
| — | `ends_at` | Thời điểm hết hiệu lực. |
| — | `active` | Trạng thái kích hoạt. |
| — | `created_at` | Thời điểm tạo. |
| — | `updated_at` | Thời điểm cập nhật gần nhất. |

### `coupon_usages`

Ghi nhận việc một user sử dụng một coupon cho một order.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `coupon_id` | Tham chiếu coupon. |
| FK | `user_id` | Tham chiếu người dùng. |
| FK, UQ | `order_id` | Tham chiếu đơn hàng. |
| — | `discount_amount` | Số tiền giảm thực tế được ghi nhận. |
| — | `status` | Trạng thái nghiệp vụ của bản ghi. |
| — | `created_at` | Thời điểm tạo. |
| — | `updated_at` | Thời điểm cập nhật gần nhất. |

## 9. Inventory

### `stock_movements`

Nhật ký biến động tồn kho của từng product variant.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `variant_id` | Tham chiếu biến thể sản phẩm. |
| — | `type` | Loại biến động kho. |
| — | `delta` | Mức thay đổi tồn kho, có thể âm hoặc dương. |
| — | `quantity_before` | Tồn kho trước biến động. |
| — | `quantity_after` | Tồn kho sau biến động. |
| — | `reference_type` | Loại chứng từ/nghiệp vụ nguồn. |
| — | `reference_id` | ID tham chiếu nghiệp vụ nguồn; không phải FK cố định. |
| UQ | `operation_key` | Khóa duy nhất giúp chống xử lý biến động kho lặp. |
| — | `note` | Ghi chú nghiệp vụ. |
| FK | `created_by_user_id` | Người tạo/thực hiện nghiệp vụ. |
| — | `created_at` | Thời điểm tạo. |

## 10. Procurement

### `suppliers`

Thông tin nhà cung cấp.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| — | `name` | Tên hiển thị. |
| — | `phone` | Số điện thoại. |
| — | `email` | Email liên hệ. |
| — | `address` | Địa chỉ. |
| — | `status` | Trạng thái nghiệp vụ của bản ghi. |
| — | `created_at` | Thời điểm tạo. |
| — | `updated_at` | Thời điểm cập nhật gần nhất. |

### `purchase_orders`

Phiếu nhập hàng từ nhà cung cấp.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `supplier_id` | Tham chiếu nhà cung cấp. |
| FK | `created_by_user_id` | Người tạo/thực hiện nghiệp vụ. |
| — | `status` | Trạng thái nghiệp vụ của bản ghi. |
| — | `note` | Ghi chú nghiệp vụ. |
| — | `created_at` | Thời điểm tạo. |
| — | `received_at` | Thời điểm nhận hàng. |

### `purchase_order_items`

Các dòng biến thể sản phẩm cần nhập trong một phiếu nhập.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK | `purchase_order_id` | Tham chiếu phiếu nhập. |
| FK | `variant_id` | Tham chiếu biến thể sản phẩm. |
| — | `quantity` | Số lượng đặt nhập của dòng phiếu. |
| — | `received_quantity` | Số lượng nhận lũy kế qua các lần receive. |
| — | `unit_cost` | Đơn giá nhập. |

## 11. Review

### `reviews`

Đánh giá của khách hàng gắn với một order item đã mua.

| Ràng buộc | Thuộc tính | Ý nghĩa |
|---|---|---|
| PK | `id` | Định danh bản ghi. |
| FK, UQ | `order_item_id` | Tham chiếu dòng hàng đã mua. |
| — | `rating` | Điểm đánh giá. |
| — | `comment` | Nội dung đánh giá. |
| — | `visible` | Trạng thái hiển thị đánh giá. |
| — | `created_at` | Thời điểm tạo. |
| — | `updated_at` | Thời điểm cập nhật gần nhất. |

## 12. Các mối quan hệ giữa các thực thể

| Thực thể A | Thực thể B | Bội số | FK / bảng liên kết | Ý nghĩa |
|---|---|---|---|---|
| `users` | `user_roles` | **1 : N** | `user_roles.user_id` (PK/FK) | Một user có nhiều bản ghi gán role. |
| `roles` | `user_roles` | **1 : N** | `user_roles.role_id` (PK/FK) | Một role được gán cho nhiều user. |
| `users` | `addresses` | **1 : N** | `addresses.user_id` | Một user có thể lưu nhiều địa chỉ. |
| `users` | `refresh_tokens` | **1 : N** | `refresh_tokens.user_id` | Một user có thể có nhiều refresh token/phiên. |
| `brands` | `products` | **1 : N** | `products.brand_id` | Một brand có nhiều product. |
| `categories` | `products` | **1 : N** | `products.category_id` | Một category có nhiều product. |
| `products` | `product_variants` | **1 : N** | `product_variants.product_id` | Một product có nhiều biến thể. |
| `products` | `product_images` | **1 : N** | `product_images.product_id` | Một product có nhiều hình ảnh. |
| `products` | `product_specifications` | **1 : N theo khóa hiện tại** | `product_specifications.product_id` | ERD chỉ đánh FK, chưa đánh UQ; vì vậy DB chưa ép một product chỉ có một specification. |
| `users` | `carts` | **1 : 0..1** | `carts.user_id` (FK, UQ) | Một user có tối đa một cart. |
| `carts` | `cart_items` | **1 : N** | `cart_items.cart_id` | Một cart có nhiều cart item. |
| `product_variants` | `cart_items` | **1 : N** | `cart_items.variant_id` | Một variant có thể xuất hiện trong nhiều cart. |
| `users` | `orders` | **1 : N** | `orders.user_id` | Một user có thể có nhiều order. |
| `orders` | `order_items` | **1 : N** | `order_items.order_id` | Một order có nhiều dòng hàng. |
| `products` | `order_items` | **1 : N** | `order_items.product_id` | Một product có thể xuất hiện trong nhiều order item. |
| `product_variants` | `order_items` | **1 : N** | `order_items.variant_id` | Một variant có thể xuất hiện trong nhiều order item. |
| `orders` | `order_status_history` | **1 : N** | `order_status_history.order_id` | Một order có nhiều lần chuyển trạng thái. |
| `users` | `order_status_history` | **1 : N** | `order_status_history.changed_by_user_id` | Một user/staff có thể thực hiện nhiều thay đổi trạng thái. |
| `orders` | `payments` | **1 : 0..1** | `payments.order_id` (FK, UQ) | Mỗi order có tối đa một payment logic. |
| `payments` | `payment_transactions` | **1 : N** | `payment_transactions.payment_id` | Một payment có thể có nhiều transaction/lần tương tác gateway. |
| `coupons` | `coupon_usages` | **1 : N** | `coupon_usages.coupon_id` | Một coupon có nhiều lượt sử dụng. |
| `users` | `coupon_usages` | **1 : N** | `coupon_usages.user_id` | Một user có thể có nhiều lượt dùng coupon. |
| `orders` | `coupon_usages` | **1 : 0..1** | `coupon_usages.order_id` (FK, UQ) | Một order có tối đa một bản ghi sử dụng coupon. |
| `suppliers` | `purchase_orders` | **1 : N** | `purchase_orders.supplier_id` | Một supplier có nhiều phiếu nhập. |
| `users` | `purchase_orders` | **1 : N** | `purchase_orders.created_by_user_id` | Một user/staff có thể tạo nhiều phiếu nhập. |
| `purchase_orders` | `purchase_order_items` | **1 : N** | `purchase_order_items.purchase_order_id` | Một phiếu nhập có nhiều dòng hàng. |
| `product_variants` | `purchase_order_items` | **1 : N** | `purchase_order_items.variant_id` | Một variant có thể xuất hiện trong nhiều phiếu nhập. |
| `product_variants` | `stock_movements` | **1 : N** | `stock_movements.variant_id` | Một variant có nhiều bản ghi biến động kho. |
| `users` | `stock_movements` | **1 : N** | `stock_movements.created_by_user_id` | Một user/staff có thể tạo nhiều stock movement. |
| `order_items` | `reviews` | **1 : 0..1** | `reviews.order_item_id` (FK, UQ) | Một dòng hàng đã mua có tối đa một review. |

Bảng trên liệt kê đủ **30 FK vật lý**. Hai quan hệ đầu hợp thành quan hệ nghiệp vụ **users N:N roles**; không có FK trực tiếp `users.role_id` hoặc `roles.user_id`.

## 13. Diễn giải các quan hệ quan trọng

### User — Role

`users` và `roles` là quan hệ **N:N**, triển khai qua `user_roles`. Bảng `user_roles` dùng cặp `(user_id, role_id)` làm khóa chính đồng thời cả hai cột đều là khóa ngoại.

```text
users 1 ───< user_roles >─── 1 roles
          N            N
```

Điều này cho phép một tài khoản đồng thời có nhiều vai trò, ví dụ `USER` + `STAFF`, và cùng một role được gán cho nhiều tài khoản.

### Product — Variant — Specification

`products` mô tả model/dòng laptop, còn `product_variants` mô tả biến thể bán hàng cụ thể như RAM, storage, color, price, quantity và SKU. `product_specifications` chứa CPU, GPU, màn hình, pin, cân nặng và OS.

Theo baseline ERD, `product_specifications.product_id` chỉ là **FK**, nên schema thiết kế và V1 mới cho phép quan hệ 1:N. Nếu nghiệp vụ muốn tối đa một bộ thông số cho mỗi product thì cần bổ sung `UQ` cho `product_id`. Code hiện dùng `@OneToOne` + `unique = true`; đây là khác biệt triển khai được ghi ở tài liệu 11, chưa được thể hiện trong ảnh hoặc V1.

### Order và dữ liệu snapshot

`orders` lưu snapshot người nhận và địa chỉ (`recipient_name`, `recipient_phone`, `shipping_address`) cùng `coupon_code_snapshot`. `order_items` lưu snapshot `product_name`, `variant_name`, `sku`, `unit_price`. Cách này giúp lịch sử đơn hàng không thay đổi khi dữ liệu catalog hoặc profile bị sửa sau này.

### Order — Payment — Payment Transaction

`payments.order_id` là `FK, UQ`, nên một order có tối đa một payment logic. Các lần gọi/callback/retry từ gateway được ghi ở `payment_transactions`, tạo quan hệ `payments 1:N payment_transactions`.

### Coupon

`coupon_usages` là entity ghi nhận việc sử dụng coupon. Nó liên kết đồng thời với `coupons`, `users` và `orders`. Vì `coupon_usages.order_id` là `FK, UQ`, một order có tối đa một bản ghi sử dụng coupon.

### Inventory

Tồn kho hiện tại nằm ở `product_variants.quantity`. `stock_movements` đóng vai trò lịch sử/ledger biến động kho, ghi `delta`, `quantity_before`, `quantity_after` và `operation_key` duy nhất để hỗ trợ chống xử lý lặp.

`operation_key` là UQ **toàn cục cho từng movement**. Một checkout/receive gồm nhiều variant phải có key riêng cho từng movement. Ảnh không có `reserved_quantity` hay bảng reservation; việc reserve hàng theo baseline là giảm quantity và hoàn lại bằng movement bù nếu hủy/hết hạn.

`reference_type` + `reference_id` là tham chiếu nghiệp vụ đa hình; `reference_id` **không phải FK cố định** tới một bảng duy nhất.

### Review

`reviews.order_item_id` là `FK, UQ`, vì vậy một dòng hàng đã mua có tối đa một review. Từ review có thể truy ngược `order_item → order → user` và `order_item → product/variant`, nên không cần lặp lại `user_id` hoặc `product_id` trong `reviews`.

## 14. Chuỗi quan hệ nghiệp vụ chính

```text
users
 ├─< addresses
 ├─< refresh_tokens
 ├─< user_roles >─ roles
 ├─ 0..1 carts ─< cart_items >─ product_variants
 └─< orders
      ├─< order_items >─ products / product_variants
      │    └─ 0..1 reviews
      ├─< order_status_history
      ├─ 0..1 payments ─< payment_transactions
      └─ 0..1 coupon_usages >─ coupons

brands ─< products >─ categories
products ─< product_variants
products ─< product_images
products ─< product_specifications   (theo khóa hiện tại)

suppliers ─< purchase_orders ─< purchase_order_items >─ product_variants
product_variants ─< stock_movements
```

## 15. Ràng buộc ERD và quyết định triển khai

### Các ràng buộc UQ đọc được từ ảnh

14 cột UQ là: `users.email`, `roles.name`, `refresh_tokens.token_hash`, `brands.name`, `categories.name`, `product_variants.sku`, `carts.user_id`, `orders.order_code`, `orders.idempotency_key`, `payments.order_id`, `coupons.code`, `coupon_usages.order_id`, `stock_movements.operation_key`, `reviews.order_item_id`. PK ghép của `user_roles` bảo đảm một cặp user/role không trùng; từng `user_id` hoặc `role_id` riêng lẻ được phép lặp.

Ảnh **không** có UQ trên `product_specifications.product_id`, `(cart_items.cart_id, variant_id)`, `(purchase_order_items.purchase_order_id, variant_id)`, `payment_transactions.provider_transaction_id` hoặc `suppliers.email`. Cờ `addresses.is_default` và `product_images.is_primary` cũng chưa có constraint bảo đảm mỗi user/product chỉ có một dòng được đánh dấu.

Trong source, CartItem đã khai báo UQ ghép và ProductSpecification đã khai báo UQ productId. Hai ràng buộc bổ sung này phải được đối chiếu bằng quyết định thiết kế và migration mới; không sửa danh sách ràng buộc đọc từ ảnh để che khác biệt.

### Các bất biến nghiệp vụ cần được triển khai riêng

| Use case | Dữ liệu liên quan | Quy tắc cần chứng minh; ảnh chưa có CHECK/trigger tương ứng |
|---|---|---|
| Cart/checkout | cart_items, order_items, product_variants | Quantity mua > 0; giá/stock được kiểm tra lại trên server. |
| Stock movement | product_variants, stock_movements | Tồn không âm; after = before + delta; cập nhật tồn và ghi ledger cùng transaction. |
| Receive | purchase_order_items, stock_movements | 0 ≤ received_quantity ≤ quantity; receive chỉ cộng phần mới nhận, không cộng lại số lũy kế. |
| Order snapshot | orders, order_items | total_amount = subtotal − discount_amount + shipping_fee; dữ liệu snapshot không thay đổi theo catalog/address hiện tại. |
| Catalog reference | order_items.product_id/variant_id | Variant phải thuộc đúng product; hai FK độc lập chưa bảo đảm quan hệ này. |
| Coupon usage | coupon_usages.user_id/order_id/coupon_id | User của usage phải là chủ order; kiểm giới hạn/thời hạn coupon và trạng thái reserve/release trong use case. |
| Review | reviews.order_item_id, order_items, orders | Rating 1..5; actor là chủ đơn; đơn đủ điều kiện DELIVERED trước khi review. |
| Default/primary | addresses.is_default, product_images.is_primary | Tối đa một địa chỉ mặc định/ảnh chính theo parent nếu nghiệp vụ yêu cầu; kiểm trong service hoặc thêm constraint được duyệt. |

V1 mới đã bổ sung CHECK cho quantity/giá/tiền, total_amount, ledger delta, received_quantity và rating; CHECK/default/index là lựa chọn triển khai được ghi tại [README migration](../../src/main/resources/db/migration/README.md). Kiểm ownership, variant thuộc product, coupon usage và default/primary vẫn thuộc use case hoặc quyết định constraint tiếp theo. Reason khi adjustment lưu được trong stock_movements.note; ERD không có cột reason riêng ở stock movement hoặc order-status history.

### Phạm vi schema đã có migration

[V1 init](../../src/main/resources/db/migration/V1__init_schema.sql) khởi tạo đủ 25 bảng/192 cột/30 FK/14 UQ trên database trống và seed ROLE_USER/ROLE_STAFF/ROLE_ADMIN. Đã kiểm Flyway migrate, Hibernate ddl-auto=validate và fixture SQL trên PostgreSQL 17.10; không thêm UQ ngoài ERD cho specification hoặc cặp cart/variant.

Database dự án đã ghi nhận V1 identity cũ thành công và chưa được áp dụng V1 mới. V1 mới là yêu cầu gộp bootstrap cho database mới; database giữ dữ liệu cũ cần đúng V1 đã áp dụng và V2 nâng cấp riêng. Không repair checksum hoặc xóa history thay cho upgrade. Các kiểu/default/CHECK/index/delete policy của V1 mới được mô tả ở README migration; thay đổi sau bootstrap dùng version mới.

## Kết luận

ERD hiện tại mô tả đầy đủ các miền Identity, Catalog, Cart, Ordering, Payment, Promotion, Inventory, Procurement và Review của LaptopStore. Điểm đáng chú ý nhất là quan hệ `users ↔ roles` được triển khai N:N qua `user_roles`, còn các quan hệ 1:1/0..1 được biểu diễn bằng `FK + UQ` ở `carts.user_id`, `payments.order_id`, `coupon_usages.order_id` và `reviews.order_item_id`.

## Checkout/payment integration

V7 bổ sung metadata request ngoài ERD gốc: orders.checkout_payment_method (COD/VNPAY) và customer_note (500 ký tự); đây là snapshot lựa chọn checkout, không phải trạng thái payment. Idempotency key UNIQUE(user_id, idempotency_key), tối đa 200 ký tự qua API. Checkout khóa cart trước khi kiểm tra key để serialize request đồng thời, gồm method và note trong so sánh payload.

Payment → OrderPaymentService (cạnh đã có trong allowlist): lockPayableOrder khóa đơn, kiểm quyền/method và trả tổng tiền; recordVerifiedPayment xác nhận PENDING_PAYMENT → CONFIRMED, trả false với đơn đã hủy/trả. IPN lưu SUCCESS và payments.status=REQUIRES_RECONCILIATION khi tiền về sau hủy. Trạng thái payment vẫn do payments sở hữu. Hủy đơn VNPAY đã xác nhận cần quy trình đối soát/hoàn tiền, endpoint hiện tại từ chối. POST payments lặp trả payment hiện có; tạo VNPay session lặp tái dùng reference và thời hạn của attempt còn hiệu lực.
