# Mô hình dữ liệu quan hệ (RDM) — LaptopStore

> Nguồn: RDM hiện tại `LaptopStore(3).drawio`.
> Quan hệ trong tài liệu này được xác định theo **PK / FK / UQ được khai báo trong các entity**. Nếu một đường connector trong Draw.io neo nhầm vào field khác với FK, khóa dữ liệu được xem là nguồn chân lý.

## 1. Quy ước

- **PK (Primary Key):** khóa chính, định danh duy nhất một bản ghi.
- **FK (Foreign Key):** khóa ngoại, tham chiếu sang entity khác.
- **UQ (Unique):** giá trị/cặp giá trị không được trùng.
- **PK/FK:** field vừa thuộc khóa chính vừa là khóa ngoại, thường dùng trong bảng liên kết.
- **1 : N:** một bản ghi phía cha có thể liên kết với nhiều bản ghi phía con.
- **1 : 0..1:** một bản ghi phía cha có tối đa một bản ghi phía con; thường được enforce bằng `FK + UQ`.
- **N : N:** quan hệ nhiều-nhiều, phải triển khai qua bảng trung gian trong mô hình quan hệ.

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

Tổng cộng: **25 thực thể**.

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
| UQ | `name` | Tên hiển thị. |

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
| — | `quantity` | Số lượng. |
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
| — | `quantity` | Số lượng. |
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
| — | `sku` | Mã SKU. |
| — | `unit_price` | Đơn giá tại thời điểm mua. |
| — | `quantity` | Số lượng. |

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
| — | `email` | Email đăng nhập. |
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
| — | `quantity` | Số lượng. |
| — | `received_quantity` | Số lượng thực tế đã nhận. |
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
| `users` | `roles` | **N : N** | `user_roles(user_id, role_id)` | Một user có thể có nhiều role và một role có thể được gán cho nhiều user. |
| `users` | `addresses` | **1 : N** | `addresses.user_id` | Một user có thể lưu nhiều địa chỉ. |
| `users` | `refresh_tokens` | **1 : N** | `refresh_tokens.user_id` | Một user có thể có nhiều refresh token/phiên. |
| `brands` | `products` | **1 : N** | `products.brand_id` | Một brand có nhiều product. |
| `categories` | `products` | **1 : N** | `products.category_id` | Một category có nhiều product. |
| `products` | `product_variants` | **1 : N** | `product_variants.product_id` | Một product có nhiều biến thể. |
| `products` | `product_images` | **1 : N** | `product_images.product_id` | Một product có nhiều hình ảnh. |
| `products` | `product_specifications` | **1 : N theo khóa hiện tại** | `product_specifications.product_id` | RDM hiện chỉ đánh FK, chưa đánh UQ; vì vậy DB chưa ép một product chỉ có một specification. |
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

Theo khóa hiện tại, `product_specifications.product_id` chỉ là **FK**, nên database đang cho phép quan hệ 1:N. Nếu nghiệp vụ muốn đúng một bộ thông số cho mỗi product thì cần bổ sung `UQ` cho `product_id`.

### Order và dữ liệu snapshot

`orders` lưu snapshot người nhận và địa chỉ (`recipient_name`, `recipient_phone`, `shipping_address`) cùng `coupon_code_snapshot`. `order_items` lưu snapshot `product_name`, `variant_name`, `sku`, `unit_price`. Cách này giúp lịch sử đơn hàng không thay đổi khi dữ liệu catalog hoặc profile bị sửa sau này.

### Order — Payment — Payment Transaction

`payments.order_id` là `FK, UQ`, nên một order có tối đa một payment logic. Các lần gọi/callback/retry từ gateway được ghi ở `payment_transactions`, tạo quan hệ `payments 1:N payment_transactions`.

### Coupon

`coupon_usages` là entity ghi nhận việc sử dụng coupon. Nó liên kết đồng thời với `coupons`, `users` và `orders`. Vì `coupon_usages.order_id` là `FK, UQ`, một order có tối đa một bản ghi sử dụng coupon.

### Inventory

Tồn kho hiện tại nằm ở `product_variants.quantity`. `stock_movements` đóng vai trò lịch sử/ledger biến động kho, ghi `delta`, `quantity_before`, `quantity_after` và `operation_key` duy nhất để hỗ trợ chống xử lý lặp.

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

## Kết luận

RDM hiện tại mô tả đầy đủ các miền Identity, Catalog, Cart, Ordering, Payment, Promotion, Inventory, Procurement và Review của LaptopStore. Điểm đáng chú ý nhất là quan hệ `users ↔ roles` được triển khai N:N qua `user_roles`, còn các quan hệ 1:1/0..1 được biểu diễn bằng `FK + UQ` ở `carts.user_id`, `payments.order_id`, `coupon_usages.order_id` và `reviews.order_item_id`.
