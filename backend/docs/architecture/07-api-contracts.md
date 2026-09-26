# Quy ước API và lỗi

Đây là API **dự kiến**, chưa có endpoint nào trong source nghiệp vụ. Owner phải chốt request/response và quyền trước khi frontend tích hợp; OpenAPI sẽ là hợp đồng chạy được khi thêm dependency/cấu hình.

## Quy ước HTTP

Prefix `/api/v1`; public GET catalog; `/me` lấy actor từ token; `/staff` cho STAFF hoặc ADMIN; `/admin` chỉ ADMIN. Resource id không phải cơ chế phân quyền. POST tạo mới trả 201 và Location nếu thích hợp; GET/PATCH thành công trả 200; DELETE không có body trả 204.

Response thành công dùng DTO trực tiếp, danh sách dùng `PageResponse<T>`. Chưa tạo `ApiResponse<T>` wrapper chỉ vì bản tài liệu cũ có tên này; nếu đổi cần thống nhất thiết kế API đồng bộ với frontend. Phân trang page từ 0, size mặc định 20, tối đa 100; sort whitelist theo endpoint. Lỗi trang/size không hợp lệ trả 400; không truyền tùy ý tên cột vào SQL.

## Endpoint nền tảng đề xuất

| Method và path | Owner | Quyền/điều kiện |
|---|---|---|
| POST `/auth/register`, `/auth/login` | auth | Public; có rate limit |
| POST `/auth/refresh`, `/auth/logout` | auth | Refresh cookie và bảo vệ CSRF phù hợp |
| GET/PATCH `/me` | user | Đã xác thực, tài khoản hoạt động |
| GET/POST `/me/addresses` | user | Đã xác thực; ownership |
| PATCH/DELETE `/me/addresses/{id}` | user | Đã xác thực; ownership |
| GET `/products`, `/products/{id}`, `/brands`, `/categories` | catalog | Public; chỉ dữ liệu đang bán |
| POST/PATCH `/admin/products` hoặc `/admin/products/{id}` | catalog | ADMIN; không chỉnh stock trong DTO |
| GET `/me/cart`; POST `/me/cart/items`; PATCH/DELETE `/me/cart/items/{id}` | cart | USER; ownership |
| POST `/me/orders` | order | USER; Idempotency-Key bắt buộc |
| GET `/me/orders`, `/me/orders/{id}` | order | USER; ownership |
| POST `/me/orders/{id}/cancel` | order | Chủ đơn; PENDING và đủ điều kiện |
| GET `/staff/orders`; PATCH `/staff/orders/{id}/status` | order | STAFF/ADMIN; state transition |
| POST `/me/orders/{id}/payment-session` | payment | USER; ownership; online payable |
| POST `/staff/orders/{id}/cod-payment` | payment | STAFF/ADMIN; DELIVERED; chống lặp |
| Provider callback path theo adapter được chốt | payment | Xác minh provider; không dựa JWT khách |
| POST `/admin/inventory/adjustments` | inventory | ADMIN; lý do bắt buộc |
| GET `/staff/inventory/movements` | inventory | STAFF/ADMIN; phân trang |
| CRUD `/admin/suppliers` | procurement | ADMIN; ngừng hoạt động nếu đã được tham chiếu |
| POST `/staff/purchase-orders`; POST `/staff/purchase-orders/{id}/receive` | procurement | STAFF/ADMIN; state/idempotency |
| CRUD `/admin/coupons`; POST `/me/coupons/preview` | promotion | ADMIN quản lý; USER xem trước |
| GET `/products/{id}/reviews`; POST `/me/reviews` | review | Public GET; USER đã mua cho POST |
| PATCH `/staff/reviews/{id}/visibility` | review | STAFF/ADMIN; lý do kiểm duyệt |
| GET `/admin/users`; PATCH `/admin/users/{id}/status`, `/roles` | user | ADMIN; ghi audit thông tin thay đổi |

Các path trong bảng đều nối với `/api/v1`; `{id}` của user roles là `/admin/users/{id}/roles`. Đây là danh sách khởi đầu, không tuyên bố đầy đủ tất cả CRUD.

## Ví dụ checkout

Header `Idempotency-Key: checkout-example-001`. Request không nhận giá/tổng/userId:

```json
{
  "addressId": 15,
  "paymentMethod": "COD",
  "couponCode": "STUDENT10"
}
```

Response 201 minh họa:

```json
{
  "id": 125,
  "orderCode": "ORD-20260918-000125",
  "status": "PENDING",
  "paymentStatus": "UNPAID",
  "subtotal": 24990000,
  "discountAmount": 500000,
  "shippingFee": 0,
  "totalAmount": 24490000,
  "createdAt": "2026-09-18T10:30:00Z"
}
```

`paymentStatus` là field DTO được suy ra từ `payments.status`, không phải cột của `orders`. `paymentMethod` được lưu ở `payments.method`.

Retry cùng key/payload trả order cũ với 200; cùng key khác payload trả 409. BigDecimal được serialize dạng số JSON theo contract MVP VND; frontend không dùng số do nó tính làm số tiền authoritative.

## Response lỗi thống nhất

```json
{
  "timestamp": "2026-09-18T10:31:02Z",
  "status": 409,
  "code": "INSUFFICIENT_STOCK",
  "message": "Sản phẩm không đủ tồn kho",
  "path": "/api/v1/me/orders",
  "traceId": "example-trace-id",
  "fieldErrors": []
}
```

| Status | Code điển hình | Ý nghĩa |
|---|---|---|
| 400 | VALIDATION_ERROR | Request sai hình dạng/giá trị cơ bản |
| 401 | UNAUTHENTICATED | Thiếu/sai/hết hạn credential |
| 403 | ACCESS_DENIED | Có danh tính nhưng sai role |
| 404 | RESOURCE_NOT_FOUND | Không tồn tại hoặc tài nguyên cá nhân không thuộc actor |
| 409 | INSUFFICIENT_STOCK, PRODUCT_CHANGED, INVALID_ORDER_STATE, DUPLICATE_RESOURCE, IDEMPOTENCY_CONFLICT | Xung đột dữ liệu/trạng thái |
| 422 | COUPON_NOT_APPLICABLE | Không đáp ứng điều kiện coupon |
| 429 | RATE_LIMITED | Quá tần suất |
| 500 | INTERNAL_ERROR | Lỗi ngoài dự kiến, không trả chi tiết nội bộ |

GlobalExceptionHandler map lỗi MVC; security EntryPoint/AccessDeniedHandler dùng cùng cấu trúc cho filter. Không trả raw exception, SQL, token hoặc stack trace. Mã lỗi ổn định để frontend xử lý, message cho người dùng.
