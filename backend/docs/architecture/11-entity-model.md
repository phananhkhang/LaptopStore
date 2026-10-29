# Entity model theo ERD

Tài liệu này ánh xạ trực tiếp từ [`database/LaptopStore_ERD.png`](../../../database/LaptopStore_ERD.png). Tên bên trái là tên cột trong ERD; tên trong ngoặc là tên field Java khuyến nghị. Không tự thêm cột, khóa hoặc quan hệ chưa xuất hiện trong ERD. Danh sách đầy đủ theo dạng bảng và toàn bộ bội số quan hệ nằm tại [12 ERD LaptopStore](12-ERD-LaptopStore.md).

**Rà soát:** 2026-10-01. ERD gồm **25 bảng, 192 cột, 30 FK và 14 ràng buộc UQ ngoài PK**. Có 24 bảng dùng PK `id`; `user_roles` dùng PK ghép `(user_id, role_id)`.

Các mục 1–9 mô tả **baseline ERD**. Kiểu dữ liệu, ánh xạ JPA hiện có và các điểm lệch được tách riêng ở mục 10–12. [13 Phân quyền và chức năng](13-roles-and-functions.md) mô tả use case/quyền; danh sách chức năng không tự bổ sung bảng hoặc cột vào mô hình này.

## 1. Identity & Access

### User

`users`: `id`, `email`, `password_hash` (`passwordHash`), `full_name` (`fullName`), `phone`, `token_version` (`tokenVersion`), `status`, `created_at` (`createdAt`), `updated_at` (`updatedAt`).

- PK: `id`.
- UQ: `email`.
- Quan hệ: một user có nhiều address, refresh token, order, coupon usage, order-status history, stock movement và purchase order; có tối đa một cart; có nhiều role qua `user_roles`.
- `password_hash` không được đưa ra DTO/API.

### Role và UserRole

`roles`: `id`, `name`; PK `id`, UQ `name`.

`user_roles`: `user_id` (`userId`), `role_id` (`roleId`); cả hai cột cùng là PK ghép và đồng thời là FK.

`users` và `roles` là quan hệ N:N qua `user_roles`. Có thể ánh xạ bảng nối bằng `@ManyToMany` nếu không cần entity trung gian, nhưng schema vẫn phải giữ PK ghép `(user_id, role_id)`.

Code hiện dùng `User.roles: Set<Role>` với `@ManyToMany`/`@JoinTable`; không có class `UserRole` riêng. Migration V1 seed `ROLE_USER`, `ROLE_STAFF`, `ROLE_ADMIN`; đây là dữ liệu triển khai, không phải tập giá trị được ghi trên ảnh ERD.

### Address

`addresses`: `id`, `user_id` (`userId`), `recipient_name` (`recipientName`), `phone`, `ward`, `district`, `city`, `street`, `postal_code` (`postalCode`), `is_default` (`isDefault`), `created_at` (`createdAt`), `updated_at` (`updatedAt`).

- PK: `id`; FK: `user_id -> users.id`.
- Một user có thể có nhiều address.
- Khi checkout, ghép các thành phần địa chỉ thành snapshot `orders.shipping_address`; không dựng lại hóa đơn cũ từ address hiện tại.

### RefreshToken

`refresh_tokens`: `id`, `user_id` (`userId`), `token_hash` (`tokenHash`), `family_id` (`familyId`), `expires_at` (`expiresAt`), `used_at` (`usedAt`), `revoked_at` (`revokedAt`), `created_at` (`createdAt`).

- PK: `id`; FK: `user_id -> users.id`; UQ: `token_hash`.
- Một user có thể có nhiều refresh token.

## 2. Catalog

### Brand

`brands`: `id`, `name`, `logo_url` (`logoUrl`), `description`, `active`.

- PK: `id`; UQ: `name`.
- Một brand có thể có nhiều product.

### Category

`categories`: `id`, `name`, `description`, `active`.

- PK: `id`; UQ: `name`.
- Một category có thể có nhiều product.

### Product

`products`: `id`, `brand_id` (`brandId`), `category_id` (`categoryId`), `name`, `description`, `status`, `created_at` (`createdAt`), `updated_at` (`updatedAt`).

- PK: `id`.
- FK: `brand_id -> brands.id`, `category_id -> categories.id`.
- Một product có nhiều variant, image, specification và order item.

### ProductVariant

`product_variants`: `id`, `product_id` (`productId`), `ram`, `storage`, `color`, `price`, `quantity`, `sku`, `status`, `created_at` (`createdAt`), `updated_at` (`updatedAt`).

- PK: `id`; FK: `product_id -> products.id`; UQ: `sku`.
- `quantity` là tồn hiện tại theo đúng ERD; không có bảng `inventory_stocks`.
- Một variant có thể xuất hiện trong nhiều cart item, order item, stock movement và purchase-order item.

### ProductImage

`product_images`: `id`, `product_id` (`productId`), `image_url` (`imageUrl`), `is_primary` (`isPrimary`).

- PK: `id`; FK: `product_id -> products.id`.
- ERD không khai báo `alt_text`, `sort_order` hay unique constraint bổ sung.

### ProductSpecification

`product_specifications`: `id`, `product_id` (`productId`), `cpu`, `gpu`, `screen_size` (`screenSize`), `screen_resolution` (`screenResolution`), `refresh_rate` (`refreshRate`), `battery`, `weight`, `os`.

- PK: `id`; FK: `product_id -> products.id`.
- Vì `product_id` không có UQ trong ERD, quan hệ được schema hiện tại biểu diễn là Product 1:N ProductSpecification, không phải 1:1.

## 3. Cart

### Cart

`carts`: `id`, `user_id` (`userId`), `created_at` (`createdAt`), `updated_at` (`updatedAt`).

- PK: `id`; `user_id` vừa là FK tới `users.id` vừa là UQ.
- Một user có tối đa một cart; một cart có nhiều cart item.

### CartItem

`cart_items`: `id`, `cart_id` (`cartId`), `variant_id` (`variantId`), `quantity`, `created_at` (`createdAt`), `updated_at` (`updatedAt`).

- PK: `id`.
- FK: `cart_id -> carts.id`, `variant_id -> product_variants.id`.
- ERD không khai báo UQ `(cart_id, variant_id)`.

## 4. Ordering

### Order

`orders`: `id`, `user_id` (`userId`), `order_code` (`orderCode`), `status`, `subtotal`, `discount_amount` (`discountAmount`), `shipping_fee` (`shippingFee`), `total_amount` (`totalAmount`), `recipient_name` (`recipientName`), `recipient_phone` (`recipientPhone`), `shipping_address` (`shippingAddress`), `coupon_code_snapshot` (`couponCodeSnapshot`), `idempotency_key` (`idempotencyKey`), `created_at` (`createdAt`), `updated_at` (`updatedAt`).

- PK: `id`; FK: `user_id -> users.id`.
- UQ riêng: `order_code`, `idempotency_key`.
- Một order có nhiều order item và status-history row; có tối đa một payment và tối đa một coupon usage.
- ERD không đặt `payment_method`, `payment_status`, `request_hash` hoặc `note` trong `orders`.

### OrderItem

`order_items`: `id`, `order_id` (`orderId`), `product_id` (`productId`), `variant_id` (`variantId`), `product_name` (`productName`), `variant_name` (`variantName`), `sku`, `unit_price` (`unitPrice`), `quantity`.

- PK: `id`.
- FK: `order_id -> orders.id`, `product_id -> products.id`, `variant_id -> product_variants.id`.
- Một order item có tối đa một review vì `reviews.order_item_id` là UQ.
- ERD không có `line_total` hoặc timestamps trong `order_items`.

### OrderStatusHistory

`order_status_history`: `id`, `order_id` (`orderId`), `from_status` (`fromStatus`), `to_status` (`toStatus`), `changed_at` (`changedAt`), `changed_by_user_id` (`changedByUserId`).

- PK: `id`.
- FK: `order_id -> orders.id`, `changed_by_user_id -> users.id`.
- ERD không có cột `reason`.

## 5. Payment

### Payment

`payments`: `id`, `order_id` (`orderId`), `method`, `amount`, `status`, `created_at` (`createdAt`), `updated_at` (`updatedAt`), `expires_at` (`expiresAt`).

- PK: `id`; `order_id` vừa là FK tới `orders.id` vừa là UQ.
- Một order có tối đa một logical payment; một payment có nhiều payment transaction.
- ERD không có `currency` hoặc `reconciliation_status`.

### PaymentTransaction

`payment_transactions`: `id`, `payment_id` (`paymentId`), `provider`, `provider_transaction_id` (`providerTransactionId`), `provider_reference` (`providerReference`), `amount`, `status`, `created_at` (`createdAt`).

- PK: `id`; FK: `payment_id -> payments.id`.
- ERD không đánh UQ cho provider transaction và không có `type` hoặc `raw_result_code`.

## 6. Promotion

### Coupon

`coupons`: `id`, `code`, `discount_type` (`discountType`), `discount_value` (`discountValue`), `max_discount` (`maxDiscount`), `min_order_amount` (`minOrderAmount`), `usage_limit` (`usageLimit`), `usage_per_user` (`usagePerUser`), `starts_at` (`startsAt`), `ends_at` (`endsAt`), `active`, `created_at` (`createdAt`), `updated_at` (`updatedAt`).

- PK: `id`; UQ: `code`.
- Một coupon có nhiều coupon usage.

### CouponUsage

`coupon_usages`: `id`, `coupon_id` (`couponId`), `user_id` (`userId`), `order_id` (`orderId`), `discount_amount` (`discountAmount`), `status`, `created_at` (`createdAt`), `updated_at` (`updatedAt`).

- PK: `id`.
- FK: `coupon_id -> coupons.id`, `user_id -> users.id`, `order_id -> orders.id`.
- `order_id` đồng thời là UQ, nên một order có tối đa một coupon usage.

## 7. Inventory

### StockMovement

`stock_movements`: `id`, `variant_id` (`variantId`), `type`, `delta`, `quantity_before` (`quantityBefore`), `quantity_after` (`quantityAfter`), `reference_type` (`referenceType`), `reference_id` (`referenceId`), `operation_key` (`operationKey`), `note`, `created_by_user_id` (`createdByUserId`), `created_at` (`createdAt`).

- PK: `id`.
- FK: `variant_id -> product_variants.id`, `created_by_user_id -> users.id`.
- UQ: `operation_key` (toàn cục, không phải khóa ghép với `variant_id`).
- `reference_id` là tham chiếu nghiệp vụ đa hình, không phải FK cố định trong ERD.
- Tồn hiện tại nằm ở `product_variants.quantity`; stock movement là lịch sử biến động.

## 8. Procurement

### Supplier

`suppliers`: `id`, `name`, `phone`, `email`, `address`, `status`, `created_at` (`createdAt`), `updated_at` (`updatedAt`).

- PK: `id`.
- Một supplier có nhiều purchase order.
- ERD không khai báo `code`, `active` hoặc UQ trên email.

### PurchaseOrder

`purchase_orders`: `id`, `supplier_id` (`supplierId`), `created_by_user_id` (`createdByUserId`), `status`, `note`, `created_at` (`createdAt`), `received_at` (`receivedAt`).

- PK: `id`.
- FK: `supplier_id -> suppliers.id`, `created_by_user_id -> users.id`.
- Một purchase order có nhiều purchase-order item.

### PurchaseOrderItem

`purchase_order_items`: `id`, `purchase_order_id` (`purchaseOrderId`), `variant_id` (`variantId`), `quantity`, `received_quantity` (`receivedQuantity`), `unit_cost` (`unitCost`).

- PK: `id`.
- FK: `purchase_order_id -> purchase_orders.id`, `variant_id -> product_variants.id`.
- ERD không khai báo UQ `(purchase_order_id, variant_id)`.

## 9. Review

### Review

`reviews`: `id`, `order_item_id` (`orderItemId`), `rating`, `comment`, `visible`, `created_at` (`createdAt`), `updated_at` (`updatedAt`).

- PK: `id`; `order_item_id` vừa là FK tới `order_items.id` vừa là UQ.
- Một order item có tối đa một review.
- ERD không lặp `user_id`, `product_id`, `variant_id` trong review; các dữ liệu này truy ngược qua order item.

## Nguyên tắc ánh xạ Java

- Quan hệ JPA chỉ nên được dùng trong cùng module. Với FK xuyên module, entity giữ ID và module giao tiếp qua service/DTO contract.
- Không suy diễn UQ từ nghiệp vụ. Chỉ các cột được đánh UQ trong ERD mới được mô tả là unique trong baseline.
- Nếu cần thêm field/constraint ngoài ERD, phải cập nhật ERD và tài liệu 12 trước hoặc cùng thay đổi schema.

## 10. Kiểu dữ liệu và tên field Java

Ảnh ERD không ghi kiểu SQL, độ dài, precision/scale, nullability hoặc default. Bảng dưới phân biệt kiểu đang có trong source/migration với hướng thống nhất; không coi các kiểu này là thông tin đọc được từ ảnh.

| Nhóm cột ERD | Kiểu Java / triển khai hiện có | Diễn giải |
|---|---|---|
| `id` của 24 bảng; các FK ID nội bộ | `Long`; V1 dùng `BIGSERIAL`/`BIGINT` | FK cùng module có thể được biểu diễn bằng object JPA, xem mục 11. Actor trong OrderStatusHistory đã đồng bộ thành Long changedByUserId. |
| `users.token_version` | `Integer`; V1 `INT NOT NULL DEFAULT 0` | Phiên bản xác thực, không phải số lượng token. |
| `product_variants.quantity`, `cart_items.quantity`, `order_items.quantity`, `purchase_order_items.quantity`, `received_quantity` | `Integer` | Lần lượt là tồn hiện tại, số lượng trong giỏ/đơn, số lượng đặt nhập và số lượng nhận lũy kế. |
| `stock_movements.delta`, `quantity_before`, `quantity_after` | `Integer` | `delta` có thể âm; quan hệ nghiệp vụ là `quantity_after = quantity_before + delta`. |
| `coupons.usage_limit`, `usage_per_user`; `reviews.rating` | `Integer` | Giới hạn lượt dùng và điểm review; ảnh không cung cấp CHECK hay ý nghĩa của giá trị null. |
| `product_variants.price`, `orders.subtotal/discount_amount/shipping_fee/total_amount`, `order_items.unit_price`, `payments.amount`, `payment_transactions.amount`, `coupons.discount_value/max_discount/min_order_amount`, `coupon_usages.discount_amount`, `purchase_order_items.unit_cost` | `BigDecimal`; V1 dùng `NUMERIC(19,2)` | V1 giữ hai chữ số thập phân tương thích source; annotation payment/promotion hiện vẫn là precision 15, scale 2. Quy ước VND NUMERIC(19,0) trong [05 Dữ liệu](05-data-and-consistency.md) vẫn là đề xuất, chưa áp dụng. |
| `brands.active`, `categories.active`, `coupons.active`, `addresses.is_default`, `product_images.is_primary`, `reviews.visible` | `Boolean`; V1 dùng `BOOLEAN` cho `is_default` | Không ánh xạ các cờ này thành chuỗi `ACTIVE/INACTIVE`. Field Java tương ứng gồm `isDefault`, `isPrimary`. |
| `refresh_tokens.family_id` | `UUID`; V1 `UUID NOT NULL` | Định danh family phục vụ refresh-token rotation. |
| `created_at`, `updated_at`, `expires_at`, `used_at`, `revoked_at`, `starts_at`, `ends_at`, `changed_at`, `received_at` | `Instant` trong user/auth/cart/order/payment/promotion/review; `LocalDateTime` trong catalog/inventory/procurement | V1 dùng TIMESTAMPTZ cho Instant, TIMESTAMP cho LocalDateTime để Hibernate validate khớp entity. Hướng thống nhất Instant/UTC chưa áp dụng toàn bộ source. |
| `orders.status`, `order_status_history.from_status/to_status` | `OrderStatus`, lưu `EnumType.STRING` | Enum hiện có: `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPING`, `DELIVERED`, `CANCELLED`, `RETURNED`; phân biệt với state machine đề xuất trong tài liệu 13. |
| Các cột `status` còn lại, `method`, `type`, `reference_type`, `discount_type` | `String` | Tập giá trị thuộc contract nghiệp vụ, không suy ra từ tên cột. |
| `stock_movements.reference_id` | `String referenceId`; V1 VARCHAR(255) | Tham chiếu nghiệp vụ đa hình. Nếu dùng ID số nội bộ cần chốt lại contract trước khi đổi kiểu. |
| `payment_transactions.provider_transaction_id` | `Long providerTransactionId`; V1 BIGINT | Là mã bên ngoài, không phải FK/PK nội bộ. Chỉ dùng Long khi contract provider bảo đảm mã số; mã chứa ký tự cần String và migration mới. |
| Các cột còn lại: email, hash, tên, số điện thoại, địa chỉ, URL, mô tả, mã/SKU, snapshot, note/comment, RAM/storage/color, CPU/GPU/màn hình/pin/weight/OS | `String` | Điện thoại, postal code và mã nghiệp vụ không chuyển sang số. Thông số kỹ thuật hiện là chuỗi, kể cả `screenSize`, `refreshRate`, `weight`. |

`user_roles` chỉ có hai field khóa kiểu `Long` nếu chọn entity trung gian. Collection như `User.roles`, `Product.variants/images`, `Cart.items`, `PurchaseOrder.items` là thuộc tính điều hướng Java, không phải cột mới trong ERD.

## 11. Ánh xạ FK đang có trong source

Tên `productId`, `cartId`, ... ở phần baseline là tên scalar khuyến nghị khi giữ ID. Trong cùng module, code có thể dùng field object với `@JoinColumn` tương ứng. Bảng này mô tả source tại ngày rà soát, không khẳng định FK đã tồn tại trên database đang chạy.

| Bảng / FK trong ERD | Field Java hiện có | Cách ánh xạ |
|---|---|---|
| `user_roles.user_id`, `role_id` | `User.roles: Set<Role>` | `@ManyToMany`, `@JoinTable`; PK ghép được V1 tạo. |
| `addresses.user_id` | `Address.userId: Long` | Scalar; V1 tạo FK tới users. |
| `refresh_tokens.user_id` | `RefreshToken.userId: Long` | Scalar xuyên auth → user; V1 tạo FK. |
| `products.brand_id`, `category_id` | `Product.brand: Brand`, `category: Category` | Hai `@ManyToOne` trong catalog. |
| `product_variants.product_id`, `product_images.product_id` | `ProductVariant.product`, `ProductImage.product` | `@ManyToOne`; phía Product có collection variants/images. |
| `product_specifications.product_id` | `ProductSpecification.product`; `Product.specification` | Hiện `@OneToOne` + UQ, lệch baseline 1:N. |
| `carts.user_id` | `Cart.userId: Long` | Scalar xuyên module, UQ. |
| `cart_items.cart_id`, `variant_id` | `CartItem.cart: Cart`, `variantId: Long` | `@ManyToOne` trong cart; variant giữ ID xuyên module. |
| `orders.user_id` | `Order.userId: Long` | Scalar xuyên module. |
| `order_items.order_id`, `product_id`, `variant_id` | `OrderItem.order: Order`, `productId: Long`, `variantId: Long` | Order là `@ManyToOne`; catalog giữ ID. |
| `order_status_history.order_id`, `changed_by_user_id` | `OrderStatusHistory.order: Order`, `changedByUserId: Long` | Order là @ManyToOne; actor giữ ID xuyên module, đúng cột ERD. |
| `payments.order_id` | `Payment.orderId: Long` | Scalar xuyên module, UQ. |
| `payment_transactions.payment_id` | `PaymentTransaction.payment: Payment` | `@ManyToOne` trong payment. |
| `coupon_usages.coupon_id`, `user_id`, `order_id` | `CouponUsage.coupon: Coupon`, `userId: Long`, `orderId: Long` | Coupon là `@ManyToOne`; user/order giữ ID, orderId UQ. |
| `stock_movements.variant_id`, `created_by_user_id` | `StockMovement.variant: ProductVariant`, `createdByUserId: Long` | Variant hiện là JPA xuyên module, cần chuyển sang ID theo kiến trúc; actor đã giữ ID. |
| `purchase_orders.supplier_id`, `created_by_user_id` | `PurchaseOrder.supplier: Supplier`, `createdByUserId: Long` | Supplier là `@ManyToOne` trong procurement; actor giữ ID. |
| `purchase_order_items.purchase_order_id`, `variant_id` | `PurchaseOrderItem.purchaseOrder: PurchaseOrder`, `variant: ProductVariant` | Quan hệ PO cùng module; variant hiện là JPA xuyên module, cần chuyển sang ID. |
| `reviews.order_item_id` | `Review.orderItemId: Long` | Scalar xuyên module, UQ. Owner/product được truy qua order item, không thêm userId/productId vào review. |

## 12. Kết quả đối chiếu và các điểm chưa đồng bộ

Danh sách bảng/cột ở mục 1–9 khớp ảnh ERD hiện có; **không có bảng/cột mới từ ảnh cần bổ sung**. Source có **24 class `@Entity`** cho 24 bảng chính và bảng nối `user_roles` qua `@JoinTable`, tương ứng 25 bảng. `InventoryStock` chỉ là class rỗng, không có `@Entity`/`@Table` và không tạo bảng `inventory_stocks`.

| Vị trí | ERD / kiến trúc chuẩn | Hiện có trong source | Việc cần đồng bộ khi sửa code/schema |
|---|---|---|---|
| [ProductSpecification](../../src/main/java/org/akira/laptopstore/catalog/entity/ProductSpecification.java), [Product](../../src/main/java/org/akira/laptopstore/catalog/entity/Product.java) | `product_id` chỉ FK; Product 1:N Specification | `@OneToOne`, `unique = true`; `Product.specification` đơn | Theo ERD thì dùng `@ManyToOne`/collection. Nếu chọn 1:1, cập nhật ERD và migration thêm UQ như quyết định thiết kế mới. |
| [CartItem](../../src/main/java/org/akira/laptopstore/cart/entity/CartItem.java) | ERD không có UQ `(cart_id, variant_id)` | Có `uk_cart_items_cart_variant` | Đây là ràng buộc bổ sung trong JPA; cần quyết định thiết kế và migration/ERD tương ứng trước khi gọi là baseline DB. |
| [OrderStatusHistory](../../src/main/java/org/akira/laptopstore/order/entity/OrderStatusHistory.java) | Cột `changed_by_user_id` FK tới `users.id` | Đã sửa thành Long changedByUserId, @Column(name = "changed_by_user_id") | Đã đồng bộ với V1 mới; actor nullable cho thay đổi do hệ thống. |
| [StockMovement](../../src/main/java/org/akira/laptopstore/inventory/entity/StockMovement.java), [PurchaseOrderItem](../../src/main/java/org/akira/laptopstore/procurement/entity/PurchaseOrderItem.java) | FK DB xuyên module; Java giữ `Long variantId` | Import `catalog.entity.ProductVariant` và dùng `@ManyToOne` | Chuyển quan hệ Java sang ID, đọc/ghi catalog qua service/DTO contract; FK DB vẫn được giữ. |
| Timestamps, tiền, mã provider/reference | ERD không quy định kiểu | Có cả Instant/LocalDateTime, precision/scale khác nhau, mã provider Long và referenceId String | Chốt các contract/kiểu ở mục 10; không ghi quyết định kiểu thành thông tin có trên ảnh. |
| [OrderStatus](../../src/main/java/org/akira/laptopstore/order/entity/enums/OrderStatus.java) | ERD chỉ có các cột trạng thái | Enum hiện khác state machine đề xuất ở tài liệu 13 | Đồng bộ enum, service, DTO và ràng buộc DB khi triển khai use case; không coi trạng thái đề xuất là đã chạy. |
| [V1 init](../../src/main/resources/db/migration/V1__init_schema.sql) | Baseline gồm 25 bảng, 30 FK | V1 mới khởi tạo đủ 25 bảng/192 cột/30 FK/14 UQ, seed 3 role | Đã kiểm Flyway và Hibernate validate trên PostgreSQL 17 mới. Database đã chạy V1 identity cũ cần giữ V1 cũ và migration nâng cấp riêng, không thay checksum. |

Bootstrap V1 mới và ánh xạ actor trong OrderStatusHistory đã được triển khai/kiểm thử trên database riêng. Các điểm lệch specification, UQ cart item, JPA xuyên module và state machine vẫn cần đồng bộ; kiểm thử schema không chứng minh các use case đó đã hoàn tất. Xem [quyết định V1 và database cũ](../../src/main/resources/db/migration/README.md).

## Checkout/payment integration

V7 bổ sung metadata request ngoài ERD gốc: orders.checkout_payment_method (COD/VNPAY) và customer_note (500 ký tự); đây là snapshot lựa chọn checkout, không phải trạng thái payment. Idempotency key UNIQUE(user_id, idempotency_key), tối đa 200 ký tự qua API. Checkout khóa cart trước khi kiểm tra key để serialize request đồng thời, gồm method và note trong so sánh payload.

Payment → OrderPaymentService (cạnh đã có trong allowlist): lockPayableOrder khóa đơn, kiểm quyền/method và trả tổng tiền; recordVerifiedPayment xác nhận PENDING_PAYMENT → CONFIRMED, trả false với đơn đã hủy/trả. IPN lưu SUCCESS và payments.status=REQUIRES_RECONCILIATION khi tiền về sau hủy. Trạng thái payment vẫn do payments sở hữu. Hủy đơn VNPAY đã xác nhận cần quy trình đối soát/hoàn tiền, endpoint hiện tại từ chối. POST payments lặp trả payment hiện có; tạo VNPay session lặp tái dùng reference và thời hạn của attempt còn hiệu lực.
