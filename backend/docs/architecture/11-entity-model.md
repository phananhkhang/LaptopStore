# Entity model đề xuất

Tài liệu này chốt baseline entity/schema trước khi viết Flyway. Đây là **logical model**, chưa phải Java code bắt buộc; owner có thể điều chỉnh tên field nhưng không đổi ownership/ràng buộc cốt lõi nếu chưa thống nhất nhóm.

## 1. Identity

### User

`id`, `email`, `passwordHash`, `fullName`, `phone`, `status`, `createdAt`, `updatedAt`.

- unique index trên `lower(email)`;
- password hash không xuất DTO;
- role qua bảng nối, không enum cứng trực tiếp trong user nếu dùng nhiều role.

### Role / UserRole

`Role(id, code)` với code `USER`, `STAFF`, `ADMIN`. `user_roles(user_id, role_id)` unique theo cặp.

### Address

`id`, `userId`, `recipientName`, `phone`, `line1`, `line2`, `ward`, `district`, `city`, `postalCode`, `isDefault`, timestamps.

Order chỉ dùng address để **tạo snapshot**; không dựng hóa đơn lịch sử bằng dữ liệu address hiện tại.

### RefreshToken

`id`, `userId`, `tokenHash`, `familyId`, `expiresAt`, `usedAt`, `revokedAt`, `createdAt`.

- unique token hash;
- lưu hash, không lưu raw token;
- family phục vụ rotation/reuse detection khi triển khai.

## 2. Catalog

### Brand / Category

`id`, `name`, `slug`, `active`, timestamps. Category có thể có `parentId` nếu thật sự cần phân cấp; MVP có thể để phẳng.

### Product

```text
id
brandId
categoryId
name
slug
description
status
warrantyMonths
createdAt
updatedAt
```

Product là **model chung**, không chứa RAM/SSD/color nếu các giá trị này thay đổi theo SKU.

### ProductVariant

```text
id
productId
sku
price
ramGb
storageGb
color
status
version
createdAt
updatedAt
```

- `sku` unique;
- `price >= 0`;
- `ramGb > 0`, `storageGb > 0` khi dùng;
- **không có `stockQuantity`**;
- `version` bảo vệ concurrent update metadata/giá của variant.

### ProductSpecification

Baseline 1:1 với Product:

```text
id
productId UNIQUE
cpuModel
gpuModel
screenSizeInches
screenResolution
panelType
refreshRateHz
batteryWh
weightKg
operatingSystem
extraSpecsJsonb?   # chỉ thêm khi thật sự cần
```

Các field dùng filter thường xuyên nên là cột typed thay vì key/value string.

### ProductImage

`id`, `productId`, `url`, `altText`, `sortOrder`, `isPrimary`; unique `(product_id, sort_order)`.

## 3. Inventory

### InventoryStock

```text
id
variantId UNIQUE
quantity
version
createdAt
updatedAt
```

- `quantity >= 0`;
- inventory sở hữu entity/repository này;
- Java chỉ giữ `variantId`, không `@ManyToOne ProductVariant`;
- một variant có tối đa một stock row trong mô hình một kho của MVP; missing row được hiểu là 0 cho tới lệnh inventory đầu tiên.
- baseline tạo row bằng `INSERT ... ON CONFLICT DO NOTHING` rồi `SELECT ... FOR UPDATE`/pessimistic lock để tránh race khi khởi tạo.

### StockMovement

```text
id
variantId
type
delta
quantityBefore
quantityAfter
referenceType
referenceId
operationKey
actorId
reason
createdAt
```

- unique `(operation_key, variant_id)`;
- append-only;
- `quantityAfter = quantityBefore + delta`;
- SALE âm; PURCHASE_RECEIPT/CANCEL_RESTORE dương; ADJUSTMENT bắt buộc reason.

Nếu sau này có nhiều kho, mở rộng thành `warehouses` + unique `(warehouse_id, variant_id)` thay vì sửa ProductVariant.

## 4. Cart

### Cart / CartItem

`Cart(id, userId UNIQUE, createdAt, updatedAt)`.

`CartItem(id, cartId, variantId, quantity, createdAt, updatedAt)` với unique `(cart_id, variant_id)` và `quantity > 0`.

Không lưu giá chính thức để checkout dùng lại; checkout luôn đọc giá catalog hiện tại.

## 5. Order

### Order

```text
id
orderCode UNIQUE
userId
status
paymentMethod
paymentStatus

recipientName
recipientPhone
shippingLine1
shippingLine2
shippingWard
shippingDistrict
shippingCity
shippingPostalCode

subtotal
discountAmount
shippingFee
totalAmount
couponCodeSnapshot

idempotencyKey
requestHash
note
createdAt
updatedAt
```

- unique `(user_id, idempotency_key)`;
- tất cả tổng tiền do server tính;
- snapshot địa chỉ nằm trực tiếp trên order hoặc embeddable cùng bảng, không chỉ lưu `addressId`.

### OrderItem

```text
id
orderId
productId
variantId
productName
sku
variantName
unitPrice
quantity
lineTotal
createdAt
```

Giữ ID để truy vết, nhưng UI hóa đơn cũ dùng snapshot. `variantName` có thể được tạo chuẩn từ RAM/SSD/color tại checkout.

### OrderStatusHistory

`id`, `orderId`, `fromStatus`, `toStatus`, `actorId`, `reason`, `createdAt`; append-only.

## 6. Promotion

### Coupon

`id`, `code`, `discountType`, `discountValue`, `maxDiscount`, `minOrderAmount`, `usageLimit`, `usagePerUser`, `startsAt`, `endsAt`, `active`, timestamps.

### CouponUsage

`id`, `couponId`, `userId`, `orderId`, `discountAmount`, `status`, `createdAt`, `releasedAt`; unique `(coupon_id, order_id)`.

## 7. Payment

### Payment

`id`, `orderId`, `method`, `amount`, `currency`, `status`, `reconciliationStatus`, timestamps. Trong MVP, `orderId` unique để một order có một logical payment; các attempt nằm ở `PaymentTransaction`.

### PaymentTransaction

`id`, `paymentId`, `provider`, `providerTransactionId`, `providerReference`, `type`, `amount`, `status`, `rawResultCode`, `createdAt`.

Một logical payment có thể có nhiều attempt/transaction. Không ghi đè attempt cũ để che mất lịch sử.

## 8. Procurement

### Supplier

`id`, `code`, `name`, `phone`, `email`, `address`, `active`, timestamps.

### PurchaseOrder / PurchaseOrderItem

PurchaseOrder: `id`, `code`, `supplierId`, `status`, `createdBy`, `createdAt`, `receivedAt`.

PurchaseOrderItem: `id`, `purchaseOrderId`, `variantId`, `quantity`, `unitCost`; unique `(purchase_order_id, variant_id)`. `unitCost` là snapshot giá nhập.

## 9. Review

### Review

`id`, `orderItemId UNIQUE`, `userId`, `productId`, `variantId`, `rating`, `comment`, `status/visible`, `hiddenReason`, `hiddenBy`, timestamps.

Review chỉ được tạo khi order item thuộc actor và order đã DELIVERED.

## Quan hệ Java giữa module

Ví dụ đúng:

```java
class InventoryStock {
    Long variantId;
}

class OrderItem {
    Long productId;
    Long variantId;
    String productName;
    String sku;
}
```

Không dùng:

```java
@ManyToOne
ProductVariant variant; // sai nếu ProductVariant thuộc module catalog
```

Quan hệ JPA chỉ giữ trong module, ví dụ `OrderItem -> Order` hoặc `ProductVariant -> Product` là hợp lệ.
