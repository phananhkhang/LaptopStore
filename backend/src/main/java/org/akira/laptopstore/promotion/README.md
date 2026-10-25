# Module promotion — Mã giảm giá

**Owner đề xuất:** D theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). Chưa gán thành viên thật. **Trạng thái:** skeleton hướng dẫn; các class dưới đây chưa triển khai. Thiết kế chi tiết cần được nhóm chốt khi nhận task.

## Trách nhiệm

Coupon, điều kiện áp dụng và lượt sử dụng. Không tự tính lại giỏ hoặc tạo Order.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | Coupon, CouponUsage |
| Repository | CouponRepository, CouponUsageRepository |
| Service | CouponService |
| Controller | AdminCouponController, CouponController |
| Request | CreateCouponRequest, PreviewCouponRequest |
| Response/contract | CouponResponse, CouponEvaluation |

Đây là danh sách gợi ý theo use case, không bắt buộc tạo đủ class trước khi có nhu cầu.

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** coupons, coupon_usages.

**Được gọi:** Không gọi module nghiệp vụ khác. Order truyền userId, subtotal, orderId và thời điểm server. Không gọi repository/entity module khác. Xem [allowlist và contract](../../../../../../../docs/architecture/04-dependencies.md).

## Bất biến và ràng buộc

- Chuẩn hóa code; kiểm tra active, khoảng thời gian, minimum spend, tổng lượt và giới hạn mỗi user.
- Một coupon trên một order ở MVP. discount không âm và không vượt subtotal; làm tròn theo quy ước tiền.
- Preview không giữ lượt. Checkout khóa coupon, kiểm tra lại và ghi CouponUsage trong cùng transaction order.
- `coupon_usages.order_id` là UQ nên mỗi order có tối đa một coupon usage; ERD không khai báo unique ghép `(coupon_id, order_id)`. Reserve/release phải idempotent; hủy hợp lệ trả lượt đúng một lần.
- Không tin subtotal client gửi để chốt đơn; preview chỉ mang tính tham khảo.

## API hiện có

| Endpoint | Quyền | Ghi chú |
|---|---|---|
| `POST/GET/PUT/DELETE /api/v1/admin/coupons[/{id}]` | ADMIN | DELETE: coupon đã có usage thì chỉ `active=false` (xóa mềm) |
| `PATCH /api/v1/admin/coupons/{id}/active` | ADMIN | Body `{"active": true/false}` |
| `GET /api/v1/admin/coupon-usages`, `/{id}`, `/coupon/{couponId}`; `PUT /{id}/release` | ADMIN | Release idempotent |
| `POST /api/v1/me/coupons/preview` | Đăng nhập | Body `{"code","subtotal"}`; chỉ tham khảo, không giữ lượt |
| `GET /api/v1/me/coupons/usages` | Đăng nhập | Lịch sử dùng coupon của chính user |

Mã lỗi: 400 `VALIDATION_ERROR`, 404 `RESOURCE_NOT_FOUND`, 409 `COUPON_CODE_EXISTS`, 422 `COUPON_NOT_APPLICABLE`.

## Contract với module order (bắt buộc)

- Đặt hàng: gọi `CouponService.reserveForOrder(...)` trong transaction tạo order (đã làm trong `OrderServiceImpl.createOrder`).
- **Mọi chỗ chuyển order sang `CANCELLED`** (user hủy, staff hủy, thanh toán thất bại/hết hạn) phải gọi `CouponService.releaseForOrder(orderId)` **trong cùng transaction**. Hàm idempotent nên gọi lặp an toàn. Hiện order chưa có luồng hủy nên lượt coupon chưa được trả.

## Luồng triển khai điển hình

Khóa coupon → kiểm tra điều kiện bằng dữ liệu server → tính giảm → ghi usage ACTIVE; khi hủy đổi RELEASED một lần.

## Bàn giao và kiểm tra

Các ca cần chứng minh: hết hạn; giới hạn lượt cuối bị tranh chấp; discount vượt subtotal; hủy lặp không giảm lượt quá mức. Cập nhật API, migration và test module cùng PR; ghi kết quả thật. Owner phối hợp người dùng contract khi đổi input/output/lỗi.

## Quy tắc các tầng và liên kết kiến trúc

Quy tắc và ràng buộc chi tiết cho từng tầng (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) tuân thủ theo [luật tầng](../../../../../../../docs/architecture/02-structure-and-layers.md) và [luồng nghiệp vụ](../../../../../../../docs/architecture/06-business-flows.md).
