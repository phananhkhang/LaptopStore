# Entity model theo ERD

Tài liệu này ánh xạ trực tiếp từ [`database/LaptopStore_ERD.jpg`](../../../database/LaptopStore_ERD.jpg). Tên bên trái là tên cột trong ERD; tên trong ngoặc là tên field Java khuyến nghị. Không tự thêm cột, khóa hoặc quan hệ chưa xuất hiện trong ERD. Danh sách đầy đủ theo dạng bảng và toàn bộ bội số quan hệ nằm tại [12 ERD LaptopStore](12-ERD-LaptopStore.md).

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
