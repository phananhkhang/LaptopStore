# LaptopStore — Phân quyền chức năng USER / STAFF / ADMIN

## 1. USER — Khách hàng

| Nhóm | Chức năng |
|---|---|
| **Auth** | Đăng ký, đăng nhập, đăng xuất, refresh token |
| **Tài khoản** | Xem/sửa thông tin cá nhân, đổi mật khẩu |
| **Địa chỉ** | Thêm, sửa, xóa địa chỉ; chọn địa chỉ mặc định |
| **Catalog** | Xem danh sách laptop, chi tiết sản phẩm/variant |
|  | Tìm kiếm theo tên/SKU |
|  | Lọc theo brand, category, khoảng giá, CPU, RAM, SSD... |
|  | Sắp xếp theo giá, mới nhất... |
| **Giỏ hàng** | Thêm sản phẩm, đổi số lượng, xóa sản phẩm, xem giỏ hàng |
|  | Kiểm tra lại giá và tồn kho hiện tại |
| **Coupon** | Nhập mã giảm giá, kiểm tra coupon hợp lệ, xem số tiền được giảm |
| **Checkout** | Chọn địa chỉ giao hàng |
|  | Chọn phương thức thanh toán |
|  | Xem subtotal / discount / shipping fee / total |
|  | Đặt hàng |
| **Đơn hàng** | Xem danh sách đơn của mình |
|  | Xem chi tiết đơn |
|  | Theo dõi trạng thái |
|  | Hủy đơn khi đang ở trạng thái được phép hủy |
| **Payment** | Chọn thanh toán online hoặc COD |
|  | Thực hiện thanh toán online |
|  | Xem trạng thái thanh toán |
|  | Retry thanh toán online nếu lần trước thất bại |
| **Review** | Xem review công khai |
|  | Đánh giá sản phẩm đã mua khi đơn đã `DELIVERED` |
|  | Sửa review của mình |

### USER không được

- Tự thay đổi giá hoặc tồn kho.
- Tự thay đổi trạng thái payment hoặc trạng thái vận hành/giao hàng.
- Xem đơn hàng của user khác.
- CRUD product, variant, brand, category, coupon, supplier.
- Tự gán role `STAFF` hoặc `ADMIN`.
- Tự xác nhận COD đã được thu tiền.

---

## 2. STAFF — Nhân viên vận hành

STAFF tập trung vào **xử lý hoạt động hàng ngày của cửa hàng**.

| Nhóm | Chức năng |
|---|---|
| **Dashboard** | Xem đơn chờ xử lý, đang đóng gói, đang giao, giao thất bại, chưa thanh toán |
|  | Xem sản phẩm sắp hết hàng |
| **Order** | Xem toàn bộ đơn hàng |
|  | Tìm kiếm/lọc đơn, xem chi tiết đơn |
|  | `PENDING_PAYMENT → CONFIRMED` khi đủ điều kiện |
|  | `CONFIRMED → PACKING` |
|  | `PACKING → SHIPPING` |
|  | `SHIPPING → DELIVERED` |
|  | `SHIPPING → DELIVERY_FAILED` |
|  | `DELIVERY_FAILED → SHIPPING` khi giao lại |
|  | Xem lịch sử trạng thái đơn |
| **COD** | Xác nhận đã thu tiền COD |
|  | Không được giả lập online payment thành công |
| **Payment** | Xem trạng thái payment, payment transaction và giao dịch cần đối soát |
| **Inventory** | Xem tồn kho, lịch sử nhập/xuất, sản phẩm sắp hết |
|  | Không được tự ý chỉnh số lượng tồn |
| **Procurement** | Xem supplier |
|  | Tạo purchase order ở trạng thái `DRAFT` |
|  | Thêm variant + quantity + unit cost |
|  | Sửa/hủy purchase order khi còn `DRAFT` |
|  | Xác nhận nhận hàng |
|  | Nhận một phần: `DRAFT → PARTIALLY_RECEIVED` |
|  | Nhận đủ: `PARTIALLY_RECEIVED → RECEIVED` hoặc `DRAFT → RECEIVED` |
|  | Khi receive, hệ thống tự cộng inventory theo số lượng thực nhận |
| **Catalog** | Xem product / variant / SKU để phục vụ bán hàng |
|  | Không CRUD catalog trong baseline hiện tại |
| **Review** | Xem review |
|  | Ẩn review vi phạm |
|  | Hiện lại review đã ẩn |
|  | Không sửa nội dung review của USER |
| **Customer** | Xem thông tin khách cần thiết để xử lý đơn |
|  | Không đổi role hoặc khóa tài khoản khách |

### STAFF không được

- Tạo/xóa product.
- Sửa giá.
- Quản lý role.
- Khóa/mở khóa tài khoản.
- CRUD coupon hoặc supplier.
- Chỉnh tồn kho thủ công.
- Sửa nội dung review của USER.
- Tự đánh dấu online payment thành công.

Đây là ranh giới chính giữa `STAFF` và `ADMIN`.

---

## 3. ADMIN — Quản trị viên

ADMIN quản lý **cấu hình, dữ liệu lõi và các chức năng quản trị của hệ thống**.

| Nhóm | Chức năng |
|---|---|
| **Dashboard** | Thống kê doanh thu, số đơn, giá trị đơn trung bình, sản phẩm bán chạy |
|  | Tồn kho thấp, đơn hủy/giao thất bại, thống kê payment |
| **User Management** | Xem danh sách tài khoản |
|  | Tìm kiếm/lọc USER/STAFF |
|  | Xem thông tin tài khoản |
|  | Khóa/mở khóa tài khoản |
|  | Gán/gỡ role |
|  | Tạo tài khoản STAFF |
| **Brand** | Tạo, sửa, activate/deactivate brand |
| **Category** | Tạo, sửa, activate/deactivate category |
| **Product** | Tạo laptop |
|  | Sửa tên/mô tả |
|  | Thay đổi brand/category |
|  | Bật/tắt bán |
|  | Quản lý ảnh |
| **Product Variant** | Tạo variant |
|  | Sửa SKU |
|  | Sửa RAM / storage / color |
|  | Sửa giá |
|  | Bật/tắt variant |
| **Product Specification** | Quản lý CPU/GPU |
|  | Quản lý screen size / resolution / refresh rate |
|  | Quản lý battery / weight / OS |
| **Inventory** | Xem tồn kho và stock movement |
|  | Điều chỉnh tồn thủ công |
|  | Bắt buộc nhập reason khi adjustment |
|  | Theo dõi lịch sử adjustment |
| **Supplier** | Tạo/sửa supplier |
|  | Activate/deactivate supplier |
|  | Xem lịch sử nhập từ supplier |
| **Procurement** | Có toàn bộ chức năng nhập hàng của STAFF |
| **Promotion** | Tạo/sửa/bật/tắt coupon |
|  | Thiết lập thời gian hiệu lực, min order value, discount type/value, max discount |
|  | Thiết lập tổng usage limit và `usagePerUser` |
|  | Xem lịch sử sử dụng |
| **Order** | Có toàn bộ chức năng xử lý đơn của STAFF |
| **Payment** | Xem toàn bộ transaction |
|  | Xử lý/đánh dấu các payment cần đối soát |
|  | Theo dõi lỗi payment |
| **Review** | Xem review |
|  | Ẩn/hiện review vi phạm |
|  | Không sửa nội dung review của USER |
| **Audit** | Xem lịch sử hành động ADMIN/STAFF |
|  | Xem ai chỉnh tồn, đổi trạng thái đơn, đổi role hoặc sửa dữ liệu quan trọng |

> Nếu giữ chức năng **Audit** trong phạm vi triển khai, database cần entity/table tương ứng như `audit_logs`. Nếu không triển khai trong baseline thì chuyển Audit sang phase mở rộng.

---

## 4. Ma trận quyền chính

| Chức năng | USER | STAFF | ADMIN |
|---|:---:|:---:|:---:|
| Xem catalog | ✅ | ✅ | ✅ |
| Mua hàng | ✅ | ❌ | ❌ |
| Quản lý giỏ hàng | ✅ | ❌ | ❌ |
| Xem đơn của mình | ✅ | — | — |
| Xem tất cả đơn | ❌ | ✅ | ✅ |
| Xử lý đơn | ❌ | ✅ | ✅ |
| Xác nhận COD đã thu tiền | ❌ | ✅ | ✅ |
| Review sản phẩm đã mua | ✅ | ❌ | ❌ |
| Moderate review | ❌ | ✅ | ✅ |
| Xem tồn kho | ❌ | ✅ | ✅ |
| Điều chỉnh tồn kho thủ công | ❌ | ❌ | ✅ |
| Tạo purchase order | ❌ | ✅ | ✅ |
| Receive purchase order | ❌ | ✅ | ✅ |
| CRUD supplier | ❌ | ❌ | ✅ |
| CRUD product | ❌ | ❌ | ✅ |
| CRUD brand/category | ❌ | ❌ | ✅ |
| CRUD coupon | ❌ | ❌ | ✅ |
| Quản lý user | ❌ | ❌ | ✅ |
| Gán/gỡ role | ❌ | ❌ | ✅ |
| Khóa/mở khóa tài khoản | ❌ | ❌ | ✅ |
| Audit log | ❌ | ❌ | ✅ |

> Ma trận trên thể hiện **quyền độc lập của từng role**. Nếu một account có nhiều role thì quyền thực tế là hợp của các role mà account đó sở hữu.

---

## 5. Nguyên tắc nhiều Role

LaptopStore dùng quan hệ:

```text
users
   1
   │
   N
user_roles
   N
   │
   1
roles
```

Một account có thể có nhiều role.

Ví dụ:

```text
User A
├── USER
└── STAFF
```

Khi đó User A có cả quyền mua hàng của `USER` và quyền vận hành của `STAFF`.

`roles` chỉ xác định role của account. Danh sách use case/API mà role được phép thực hiện được cấu hình trong backend, ví dụ bằng Spring Security:

```java
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public void confirmOrder(Long orderId) {
    ...
}
```

Không nên hiểu:

```text
role_id = 2
→ tự động thực hiện một chức năng cố định
```

Mà nên hiểu:

```text
role = STAFF
→ được phép gọi tập các chức năng mà backend cấp cho STAFF
```

---

## 6. Luồng mua hàng tổng thể

```text
USER
   ↓
Catalog
   ↓
Cart
   ↓
Coupon
   ↓
Checkout
   ↓
Tạo Order
   ↓
Kiểm tra + giảm/reserve inventory
   ↓
Reserve coupon
   ↓
Tạo Payment
   ↓
Thanh toán / COD
   ↓
STAFF xử lý đơn
   ↓
CONFIRMED
   ↓
PACKING
   ↓
SHIPPING
   ↓
DELIVERED
   ↓
Xác nhận COD nếu có
   ↓
USER Review
```

### Khi đơn bị hủy hoặc hết hạn

Nếu inventory đã được giảm/reserve tại checkout thì phải có luồng bù:

```text
Order CANCELLED / EXPIRED
        ↓
Restore inventory
        ↓
Tạo stock movement hoàn kho
        ↓
Release coupon reservation
```

Các thao tác restore/release phải **idempotent**, tránh job hoặc request retry làm cộng kho/giải phóng coupon nhiều lần.

---

## 7. Luồng Procurement

```text
STAFF / ADMIN
      ↓
Tạo Purchase Order
      ↓
DRAFT
      ↓
Nhận hàng
      ├── nhận một phần
      │      ↓
      │ PARTIALLY_RECEIVED
      │      ↓
      │ nhận tiếp
      │
      └── nhận đủ
             ↓
          RECEIVED
```

Mỗi lần receive:

```text
purchase_order_items.received_quantity tăng
                ↓
product_variants.quantity tăng
                ↓
stock_movements ghi nhận biến động kho
```

Không chỉnh trực tiếp tồn kho mà bỏ qua `stock_movements`.

---

## 8. Luồng quản lý theo Role

```text
ADMIN
 ├── User / Role
 ├── Brand
 ├── Category
 ├── Product
 ├── Product Variant
 ├── Product Specification
 ├── Coupon
 ├── Supplier
 ├── Procurement
 ├── Inventory Adjustment
 ├── Payment Reconciliation
 ├── Review Moderation
 └── Audit

STAFF
 ├── Order Processing
 ├── Shipping
 ├── COD
 ├── Payment Monitoring
 ├── Inventory Monitoring
 ├── Purchase Order
 ├── Customer Information for Order
 └── Review Moderation

USER
 ├── Account / Address
 ├── Catalog
 ├── Cart
 ├── Coupon
 ├── Checkout
 ├── Payment
 ├── Own Orders
 └── Own Reviews
```

---

## 9. Nguyên tắc phân quyền

```text
USER  = Mua hàng
STAFF = Vận hành cửa hàng
ADMIN = Quản trị dữ liệu và hệ thống
```

Không nên thiết kế:

```text
STAFF = ADMIN trừ quản lý tài khoản
```

Nếu làm vậy, ranh giới giữa STAFF và ADMIN sẽ không rõ.

Nên giữ:

```text
USER
→ tương tác với chức năng mua hàng của chính mình

STAFF
→ thao tác nghiệp vụ vận hành hằng ngày
→ không thay đổi dữ liệu lõi hoặc cấu hình hệ thống

ADMIN
→ quản trị dữ liệu lõi
→ thay đổi cấu hình/nghiệp vụ quản trị
→ có thể thực hiện các thao tác vận hành của STAFF
```

---

## 10. Phạm vi module liên quan

```text
auth
user
catalog
cart
order
payment
promotion
inventory
procurement
review
```

Nếu triển khai Audit có thể thêm module `audit` hoặc đặt thành phần audit trong infrastructure/common tùy kiến trúc, nhưng database vẫn cần nơi lưu audit log nếu muốn truy vấn lịch sử.

---

## 11. Quy tắc bảo vệ dữ liệu quan trọng

### Order

Không cho phép đổi trạng thái tùy ý. Các transition phải theo state machine đã định nghĩa.

```text
PENDING_PAYMENT
    ↓
CONFIRMED
    ↓
PACKING
    ↓
SHIPPING
    ↓
DELIVERED
```

Nhánh lỗi:

```text
SHIPPING
    ↓
DELIVERY_FAILED
    ↓
SHIPPING
```

### Payment

- USER không được tự chỉnh trạng thái payment.
- STAFF/ADMIN không được giả lập online payment success.
- Online payment success phải đến từ callback/verification hợp lệ của payment provider.
- Callback phải xử lý idempotent.

### Inventory

- `product_variants.quantity` là tồn kho hiện tại.
- `stock_movements` là lịch sử biến động kho.
- STAFF không chỉnh trực tiếp quantity.
- ADMIN adjustment phải có reason.
- Mọi thay đổi tồn kho phải đi cùng stock movement.

### Catalog

Không hard-delete dữ liệu đã được tham chiếu trong lịch sử đơn hàng nếu không cần thiết.

Ưu tiên `ACTIVE/INACTIVE` cho `Product`, `ProductVariant`, `Brand`, `Category`, `Supplier`, `Coupon` thay vì xóa vật lý.

### Review

- USER chỉ review sản phẩm mình đã mua và đủ điều kiện.
- STAFF/ADMIN chỉ moderation bằng hide/unhide.
- STAFF/ADMIN không sửa nội dung review của USER.

---

## 12. Kết luận

```text
USER
→ mua hàng và quản lý dữ liệu cá nhân

STAFF
→ vận hành order, payment, inventory, procurement và review

ADMIN
→ quản trị catalog, promotion, supplier, account/role, inventory adjustment và hệ thống
```

Thiết kế này giúp ba role có ranh giới rõ ràng, đồng thời vẫn hỗ trợ account nhiều role thông qua `user_roles`.
