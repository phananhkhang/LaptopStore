# LaptopStore — Phân quyền chức năng USER / STAFF / ADMIN

**Rà soát:** 2026-10-01. Nguồn mô hình dữ liệu là [ERD gốc](../../../database/LaptopStore_ERD.png), [11 Entity model](11-entity-model.md) và [12 ERD LaptopStore](12-ERD-LaptopStore.md). ERD có 25 bảng; không có bảng quyền chức năng riêng hoặc `audit_logs`.

## 0. Phạm vi thiết kế và hiện trạng

Các danh sách chức năng, ma trận quyền và state machine dưới đây là **thiết kế use case**; không phải xác nhận mọi endpoint/service đã triển khai. ERD xác định nơi lưu dữ liệu và quan hệ, nhưng không quy định tập giá trị trạng thái hoặc quyền gọi API. Chức năng có đủ bảng để lưu dữ liệu vẫn cần service, transaction, ownership và kiểm quyền.

[V1 init](../../src/main/resources/db/migration/V1__init_schema.sql) đã khởi tạo đầy đủ 25 bảng trên database kiểm thử mới; ánh xạ actor lịch sử đơn đã sửa thành changedByUserId. Việc có schema không chuyển các chức năng/state machine đề xuất bên dưới thành use case đã triển khai.

### Chức năng gắn với dữ liệu nào

| Nhóm chức năng | Bảng / trường trọng tâm | Giới hạn theo ERD |
|---|---|---|
| Account/Role/Address | users, roles, user_roles, addresses | Role N:N; địa chỉ thuộc user. Tối đa một địa chỉ mặc định chưa được ERD enforce bằng UQ. |
| Auth | users.password_hash/token_version/status, refresh_tokens | Token lưu hash, có family/expiry/used/revoked; không xuất hash/token ra profile DTO. |
| Catalog | brands, categories, products, product_variants, product_images, product_specifications | Variant chứa SKU/giá/tồn; specification là 1:N theo ERD, ảnh chính chưa có UQ theo product. |
| Cart | carts, cart_items | Tối đa một cart/user; cart item giữ variant và quantity, không chốt giá hoặc giữ tồn. |
| Order | orders, order_items, order_status_history | Snapshot người nhận/địa chỉ/tên/SKU/giá; lịch sử actor nằm ở changed_by_user_id, không có reason riêng. |
| Payment | payments, payment_transactions | Tối đa một logical payment/order; retry ghi nhiều transaction. Chưa có currency/reconciliation_status hoặc cột trạng thái thanh toán trên orders. |
| Promotion | coupons, coupon_usages | Tối đa một coupon usage/order; giới hạn và reserve/release phải kiểm bằng use case, không suy ra từ FK/UQ. |
| Inventory | product_variants.quantity, stock_movements | Không có inventory_stocks/reserved_quantity. Adjustment ghi lý do vào note; operation_key duy nhất cho từng movement. |
| Procurement | suppliers, purchase_orders, purchase_order_items | Supplier dùng status; số lượng nhận lũy kế là received_quantity; nhập nhiều lần không được cộng lại lượng đã nhận. |
| Review | reviews → order_items → orders và products/variants | Tối đa một review/order item. Owner/product lấy qua chuỗi quan hệ, không thêm user_id/product_id vào reviews. |
| Lịch sử nghiệp vụ | order_status_history, stock_movements | Truy vết đổi trạng thái đơn và biến động kho; chưa lưu audit tổng quát cho đổi role/sửa catalog. |

### Kiểm quyền hiện có trong backend

Đối chiếu [SecurityConfig](../../src/main/java/org/akira/laptopstore/security/SecurityConfig.java) và controller hiện có:

| Phạm vi | Hiện trạng đọc được từ source | So với ma trận thiết kế |
|---|---|---|
| Auth và đọc catalog | `/api/v1/auth/**` public; GET products/brands/categories public | Khách chưa đăng nhập cũng được đọc catalog. Matcher GET reviews public đã có, nhưng chưa có review endpoint được triển khai. |
| Profile/address/cart | `/api/v1/me/**` yêu cầu authenticated; UserController và CartController lấy ID từ CustomUserDetails | Ownership phải kiểm ở service. Matcher này cho mọi tài khoản đã xác thực, chưa giới hạn mua hàng/cart vào ROLE_USER. |
| Admin account | `/api/v1/admin/**` yêu cầu ADMIN; AdminUserController có `@PreAuthorize("hasRole('ADMIN')")` | Đã có API danh sách user, đổi status và role; các chức năng khác trong danh sách vẫn cần triển khai. |
| Staff routes | `/api/v1/staff/**` cho STAFF hoặc ADMIN | Có rule ở SecurityConfig; rule không đồng nghĩa toàn bộ use case vận hành đã có endpoint. |
| Ghi catalog | POST `/api/v1/products`, `/api/v1/brands`, `/api/v1/categories` | Hiện thuộc fallback authenticated và chưa có annotation ADMIN tại controller; chưa enforce giới hạn ADMIN theo thiết kế. |
| Đọc lịch sử kho | GET `/api/v1/inventory/variants/{variantId}/history` | Hiện chỉ authenticated; chưa giới hạn STAFF/ADMIN theo thiết kế. |
| Order/payment/promotion/procurement/review | Đã có entity; OrderController chưa có handler, các nhóm còn lại chưa có API chức năng | Không coi entity hoặc sơ đồ luồng dưới đây là bằng chứng use case đã hoạt động. |

Các khoảng trống trên là kết quả phân tích source, chưa được sửa trong lần cập nhật tài liệu này.

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
|  | Bắt buộc nhập lý do khi adjustment, lưu vào `stock_movements.note` |
|  | Theo dõi lịch sử adjustment |
| **Supplier** | Tạo/sửa supplier |
|  | Bật/tắt supplier thông qua `suppliers.status` |
|  | Xem lịch sử nhập từ supplier |
| **Procurement** | Có toàn bộ chức năng nhập hàng của STAFF |
| **Promotion** | Tạo/sửa/bật/tắt coupon |
|  | Thiết lập thời gian hiệu lực, min order value, discount type/value, max discount |
|  | Thiết lập tổng usage limit và `usagePerUser` |
|  | Xem lịch sử sử dụng |
| **Order** | Có toàn bộ chức năng xử lý đơn của STAFF |
| **Payment** | Xem toàn bộ transaction |
|  | Xem các payment cần đối soát từ status/transaction; quy trình lưu kết quả đối soát thuộc phần mở rộng |
|  | Theo dõi lỗi payment |
| **Review** | Xem review |
|  | Ẩn/hiện review vi phạm |
|  | Không sửa nội dung review của USER |
| **Lịch sử nghiệp vụ** | Xem ai đổi trạng thái đơn qua `order_status_history` |
|  | Xem ai chỉnh tồn/nhập/xuất qua `stock_movements` |

Audit tổng quát cho đổi role, sửa catalog và các dữ liệu khác thuộc **phase mở rộng**, vì ERD chưa có bảng ghi những sự kiện đó. Không gọi hai bảng lịch sử đơn/kho là audit đầy đủ của hệ thống.

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
| Xem lịch sử trạng thái đơn/biến động kho | ❌ | ✅ | ✅ |

> Ma trận trên thể hiện **quyền độc lập của từng role**. Nếu một account có nhiều role thì quyền thực tế là hợp của các role mà account đó sở hữu.

Xem lịch sử đơn của chính mình vẫn thuộc chức năng USER ở trên. Hàng cuối của ma trận là quyền xem lịch sử phục vụ vận hành nhiều đơn/SKU. Khách chưa đăng nhập được xem catalog và review công khai; quyền ghi vẫn theo role và ownership. Audit tổng quát chưa thuộc ma trận baseline.

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

`USER`, `STAFF`, `ADMIN` là tên nghiệp vụ trong tài liệu. Migration V1 lưu `roles.name` là `ROLE_USER`, `ROLE_STAFF`, `ROLE_ADMIN`; principal giữ authority có tiền tố `ROLE_`. Với Spring Security, dùng `hasRole('ADMIN')` hoặc `hasAuthority('ROLE_ADMIN')`, không dùng `hasRole('ROLE_ADMIN')` theo cấu hình mặc định. Không gắn quyền vào giá trị số cố định của `roles.id`.

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
Kiểm tra + giảm product_variants.quantity và ghi stock_movements
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

Luồng trên là thiết kế điều phối: các ghi order/items/history, quantity/movement, coupon usage và xóa mục cart đã mua cần transaction/khóa thống nhất. Không gọi provider khi giữ khóa DB; payment xử lý qua contract phù hợp sau đó. ERD không có bảng reservation hoặc cột reserved_quantity; giữ hàng trong thiết kế này được biểu diễn bằng giảm quantity khi checkout, không giảm lần nữa khi giao.

Nếu inventory đã được giảm tại checkout thì phải có luồng bù:

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

Reserve/release coupon dự kiến dùng `coupon_usages.status`; ERD không cung cấp sẵn tập trạng thái. Retry payment sử dụng payment logic hiện có và thêm transaction, không tạo payment thứ hai vì `payments.order_id` là UQ. `payments.expires_at` là thời hạn thanh toán; ERD không có `orders.expires_at`.

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

`received_quantity` là tổng đã nhận của từng dòng. Mỗi lần receive kiểm lượng mới > 0 và tổng sau nhận không vượt `quantity`; chỉ cộng tồn bằng lượng mới nhận. Các cập nhật item, trạng thái/thời điểm nhận phiếu, quantity và movement phải commit/rollback cùng nhau. `received_at` cần được định nghĩa nhất quán (ví dụ khi nhận đủ); ý nghĩa chính xác không được ảnh ERD quy định.

`DRAFT`, `PARTIALLY_RECEIVED`, `RECEIVED` là trạng thái đề xuất của luồng này; entity PurchaseOrder hiện lưu String, chưa có enum/migration/CHECK xác nhận tập trạng thái.

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
 ├── Payment Monitoring
 ├── Review Moderation
 └── Order / Stock History

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

Baseline chỉ có lịch sử đơn/kho trong module sở hữu. Audit tổng quát và quy trình lưu kết quả đối soát thanh toán cần thiết kế dữ liệu/contract riêng ở phase mở rộng; khi triển khai phải cập nhật ERD, tài liệu 11–12 và migration mới.

---

## 11. Quy tắc bảo vệ dữ liệu quan trọng

### Order

Không cho phép đổi trạng thái tùy ý. Các transition phải theo state machine đã định nghĩa.

State machine dưới đây là **đề xuất nghiệp vụ**. ERD chỉ có cột `status`, `from_status`, `to_status`. [OrderStatus hiện có](../../src/main/java/org/akira/laptopstore/order/entity/enums/OrderStatus.java) chưa dùng cùng tập giá trị:

| Thiết kế trong tài liệu này | Enum hiện có | Điểm cần chốt trước khi triển khai |
|---|---|---|
| `PENDING_PAYMENT` | `PENDING` | PENDING hiện chú thích chờ xác nhận; không mặc định coi hai trạng thái cùng nghĩa. |
| `CONFIRMED` | `CONFIRMED` | Online phải được payment verification hợp lệ; COD theo điều kiện xác nhận của use case. |
| `PACKING` | `PROCESSING` | Chốt tên và ý nghĩa trước khi đổi enum/API; không tự đổi dữ liệu lịch sử. |
| `SHIPPING`, `DELIVERED`, `CANCELLED` | Cùng tên | Có enum chưa có nghĩa service đã enforce transition. |
| `DELIVERY_FAILED`, `EXPIRED` | Chưa có | Chưa mô tả là trạng thái đã triển khai. |
| Chưa có luồng trả hàng trong baseline này | `RETURNED` | Chốt flow trả hàng/payment/hoàn kho riêng trước khi sử dụng. |

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
- `stock_movements.operation_key` là UQ toàn cục; một nghiệp vụ nhiều variant cấp key riêng từng movement.

### Catalog

Không hard-delete dữ liệu đã được tham chiếu trong lịch sử đơn hàng nếu không cần thiết.

Product, ProductVariant và Supplier dùng cột `status` kiểu String trong source; tập giá trị như `ACTIVE/INACTIVE` cần contract nghiệp vụ. Brand, Category và Coupon dùng cờ Boolean `active` (`true/false`). Không thêm cột status hoặc đổi kiểu active chỉ để đồng nhất tên thao tác bật/tắt.

### Review

- USER chỉ review sản phẩm mình đã mua và đủ điều kiện.
- Ownership đi qua `reviews.order_item_id → order_items.order_id → orders.user_id`; FK/UQ riêng lẻ không thay thế kiểm chủ đơn và trạng thái DELIVERED trong service.
- STAFF/ADMIN chỉ moderation bằng hide/unhide.
- STAFF/ADMIN không sửa nội dung review của USER.

### Phần mở rộng cần thay đổi mô hình

- Audit tổng quát: cần nơi lưu actor/action/target/time cho đổi role, sửa catalog và các hành động ngoài lịch sử đơn/kho.
- Đối soát payment có trạng thái/kết quả lưu riêng: cần contract và dữ liệu bổ sung; ERD hiện chưa có `reconciliation_status`.
- Giữ tồn riêng biệt với tồn hiện tại hoặc reserve có thời hạn độc lập: cần thiết kế reservation, không suy ra một bảng `inventory_stocks` từ class rỗng.

Các mục này chưa thuộc 25 bảng baseline và chưa được tự thêm vào entity/migration.

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
