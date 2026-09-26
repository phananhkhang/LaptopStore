# Luồng nghiệp vụ và trạng thái

Các luồng là thiết kế đề xuất, cần được nhóm chốt và thống nhất trước khi triển khai. Quyền ở đây bổ sung kiểm tra role, không thay kiểm tra ownership.

## Checkout

1. USER gửi addressId, paymentMethod, couponCode tùy chọn và Idempotency-Key; actorId lấy từ principal. ERD không có cột note trong `orders`.
2. Order kiểm tra key cũ trước; khóa cart và lấy items cần mua; từ chối giỏ trống.
3. User service kiểm tra địa chỉ thuộc khách. Catalog khóa các variant theo ID tăng dần và trả snapshot gồm product/variant name, SKU, giá và trạng thái đang bán.
4. Promotion khóa coupon nếu có, kiểm tra điều kiện bằng snapshot và tính subtotal, discount, shippingFee, total tại server; MVP shippingFee = 0. Tổng không âm; không nhận tổng từ client.
5. Tạo Order PENDING để có orderId trong transaction. Inventory dùng contract catalog để khóa `product_variants` theo ID tăng dần, kiểm tra `quantity` và giảm tồn. Nếu thiếu, trả 409 `INSUFFICIENT_STOCK` và rollback.
6. Inventory append SALE movement gắn orderId; order hoàn tất items/snapshot/history; promotion ghi usage; cart xóa mục đã mua. Mọi việc trong cùng transaction; order tạo sớm cũng rollback nếu bước sau thất bại.
7. Commit rồi trả order. COD chờ xác nhận/giao; online mở payment session bằng request khác. Checkout lỗi ở bất kỳ bước nào rollback toàn bộ.

```mermaid
flowchart TD
    Request[Request checkout] --> Existing{Key đã tồn tại}
    Existing -->|Cùng payload| Previous[Trả đơn cũ]
    Existing -->|Khác payload| Conflict[409 Conflict]
    Existing -->|Chưa có| Cart[Khóa cart]
    Cart --> Catalog[Khóa variant và chốt giá/SKU]
    Catalog --> Coupon[Khóa/kiểm tra coupon nếu có]
    Coupon --> Stock[Khóa product variant và trừ quantity]
    Stock --> Valid{Hợp lệ}
    Valid -->|Không| Rollback[Rollback toàn bộ]
    Valid -->|Có| Save[Ghi order snapshot + ledger + coupon + dọn giỏ]
    Save --> Commit[Commit và trả orderId]
```

## Trạng thái order

| Từ | Đến | Ai và điều kiện | Tác động |
|---|---|---|---|
| PENDING | CONFIRMED | STAFF/ADMIN; online phải PAID | History; không trừ tồn thêm |
| PENDING | CANCELLED | Chủ đơn hoặc STAFF/ADMIN; online chưa PAID | Hoàn tồn/coupon một lần |
| CONFIRMED | PACKING | STAFF/ADMIN | History |
| CONFIRMED | CANCELLED | STAFF/ADMIN, chưa bàn giao; online chưa PAID | Hoàn tồn/coupon một lần |
| PACKING | SHIPPING | STAFF/ADMIN | Ghi thông tin bàn giao |
| SHIPPING | DELIVERED | STAFF/ADMIN xác nhận giao | Không đổi stock; COD thu tiền là bước payment riêng |
| SHIPPING | DELIVERY_FAILED | STAFF/ADMIN | Giữ tồn đã trừ |
| DELIVERY_FAILED | SHIPPING | STAFF/ADMIN giao lại | Không đổi stock |
| DELIVERY_FAILED | CANCELLED | STAFF/ADMIN; xác nhận hàng đã về kho và online chưa PAID | Hoàn tồn/coupon đúng một lần |

DELIVERED và CANCELLED là trạng thái cuối trong MVP. USER chỉ tự hủy PENDING; STAFF xử lý các trường hợp khác theo bảng. Đơn online đã PAID muốn hủy/hoàn phải có quy trình hoàn tiền bổ sung, không ép vào endpoint hủy hiện tại. Mỗi lần chuyển lưu `from_status`, `to_status`, `changed_by_user_id`, `changed_at`; ERD không có cột reason trong bảng history.

## Hủy và payment callback đồng thời

Hủy khóa order, đọc trạng thái từ `payments` nếu có rồi chuyển CANCELLED, release coupon và gọi inventory restore đúng một lần. Callback khóa payment rồi order. Nếu payment đã PAID, hủy online bị chặn. Nếu hủy commit trước, callback vẫn lưu transaction nhận được nhưng không hồi sinh order đã CANCELLED; trường hợp cần đối soát phải được xử lý ở service vì ERD không có cột `reconciliation_status`.

## Thanh toán

Payment có trạng thái nghiệp vụ như PENDING/PAID/FAILED; hoàn tiền đầy đủ/partial ngoài MVP. Trạng thái và phương thức thanh toán nằm trong `payments`, không có cột tương ứng ở `orders`. COD có thể tạo payment gắn order và cập nhật `payments.status` khi thu tiền. DELIVERED đơn thuần chưa chứng minh đã thu tiền.

Online: payment kiểm tra order và số tiền → tạo attempt PENDING có idempotency key → commit → gọi provider/mô phỏng ngoài transaction → callback xác minh → ghi kết quả và thông báo order service trong cùng transaction. Callback FAILED lặp hoặc đến sau PAID không làm PAID lùi về FAILED. Payment attempt thứ hai thành công phải giữ record và đưa vào đối soát thay vì ghi đè lịch sử.

## Nhập hàng

STAFF/ADMIN tạo phiếu DRAFT cho supplier và các variant. Khi nhận: khóa phiếu → xác minh DRAFT → inventory khóa các `product_variants`, tăng quantity và ghi PURCHASE_RECEIPT → cập nhật `received_quantity`/trạng thái phiếu → commit. Gọi receive lặp không được cộng tồn lần hai. DRAFT có thể CANCELLED; RECEIVED không chỉnh sửa trực tiếp. Một dòng lỗi rollback cả phiếu.

## Coupon và review

Coupon preview không giữ lượt. Checkout kiểm tra lại dưới khóa và reserve usage; hủy hợp lệ chuyển usage RELEASED một lần. Review chỉ tạo khi order item thuộc actor và đơn DELIVERED; unique order_item_id bảo vệ hai request đồng thời. Review không quyết định lại trạng thái đơn.
