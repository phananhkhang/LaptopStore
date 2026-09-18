# Phụ thuộc và hợp đồng giữa các module

## Allowlist

Mũi tên A → B nghĩa là service A gọi contract service/DTO của B. “Không” nghĩa là không gọi module nghiệp vụ khác. Ngoài danh sách dưới đây bị cấm cho đến khi cập nhật thiết kế; hạ tầng chung được mô tả riêng.

| Module gọi | Module được gọi | Mục đích |
|---|---|---|
| auth | user | Tạo tài khoản, lấy credential/profile |
| user | Không | Nguồn người dùng và địa chỉ |
| catalog | Không | Nguồn sản phẩm, giá, tồn mức thấp |
| cart | catalog | Xác minh variant, dữ liệu hiển thị |
| order | user, cart, catalog, inventory, promotion | Điều phối checkout/hủy |
| payment | order | Đọc payable order, cập nhật kết quả đã xác minh |
| promotion | Không | Nhận các giá trị server truyền vào |
| inventory | catalog | Khóa và cập nhật stock qua service |
| procurement | catalog, inventory | Kiểm tra SKU và nhận hàng |
| review | order, catalog | Kiểm tra đã mua/giao và thông tin public |

Không có cạnh `cart → order`, `order → payment`, `catalog → inventory` hoặc `catalog → review`. Frontend bắt đầu payment sau khi nhận orderId; frontend gọi API reviews riêng. Quy trình nghiệp vụ có thể đi qua nhiều bước HTTP mà không tạo dependency Java hai chiều.

## Hạ tầng chung

- `security → user.service` để nạp principal/role/trạng thái; không gọi auth. `auth → security` chỉ cho JwtService/PasswordEncoder, nên không tạo vòng.
- Controller các module có thể dùng principal/annotation từ security. Service nhận actorId/actor context rõ, không nhận `HttpServletRequest`.
- Module được dùng common và exception cơ sở. Common không import module/exception/config/security. Exception không gọi service/repository.
- Config chỉ wiring/cấu hình; không điều phối use case. Không inject module service vào config chỉ để chạy nghiệp vụ khi startup.
- Security filter dùng EntryPoint/AccessDeniedHandler cho lỗi trước MVC; GlobalExceptionHandler không bao trùm mọi lỗi filter.

## Hợp đồng nội bộ dự kiến

Tên dưới đây là thiết kế để owner triển khai, chưa có Java class/method. DTO contract bất biến, chỉ ID và trường cần dùng; không truyền entity.

| Bên cung cấp | Method định hướng | Đầu vào và kết quả | Ràng buộc |
|---|---|---|---|
| user | `getOwnedAddress(userId, addressId)` | ID → AddressSnapshot | Không thuộc user: 404; order sao chép snapshot |
| user | `findCredentialByEmail(email)` | email chuẩn hóa → credential nội bộ | Chỉ auth/security dùng; không HTTP serialize |
| cart | `lockAndGetCheckoutCart(userId)` | user → CartSnapshot | Cùng transaction; khóa cart như thao tác sửa giỏ |
| cart | `clearPurchasedItems(userId, itemIds)` | mục đã đặt → void | Không xóa toàn bộ giỏ ngoài transaction |
| catalog | `getSellableVariants(ids)` | ids → VariantSnapshot[] | Kiểm tra active, lấy giá mới, không trả entity |
| catalog | `applyStockChanges(changes)` | variant/delta, expectedVersion khi checkout → before/after | Chỉ inventory gọi; khóa tăng dần; checkout kiểm tra version và trạng thái bán; transaction bắt buộc |
| inventory | `deductForOrder(...)`, `restoreForOrder(...)` | orderId, lines, operationKey → kết quả | Một lần mỗi key/variant; tồn và ledger atomic |
| inventory | `receivePurchase(...)` | purchaseId, lines, operationKey | Cùng transaction nhận phiếu |
| promotion | `lockAndEvaluateForCheckout(...)` | coupon, userId, subtotal → CouponEvaluation | Khóa coupon trước variants; không giữ lượt ngoài transaction checkout |
| promotion | `reserveForOrder(...)`, `releaseForOrder(...)` | coupon, userId, orderId, subtotal → CouponEvaluation | Khóa và ghi usage; không tự query order |
| order | `getPayableOrder(actorId, orderId)` | ID → OrderPaymentSnapshot | Ownership, total, method, status |
| order | `recordVerifiedPayment(result)` | kết quả nội bộ từ payment → outcome | Khóa order, cập nhật projection; CANCELLED → RECONCILIATION_REQUIRED |
| order | `getDeliveredItem(userId, orderItemId)` | ID → PurchasedItemView | Chỉ đúng chủ đơn và DELIVERED |

Lỗi chéo dùng exception cơ sở/mã lỗi đã công bố, ví dụ `INSUFFICIENT_STOCK`, `INVALID_ORDER_STATE`, `COUPON_NOT_APPLICABLE`. Caller không bắt mọi Exception rồi trả thành công. Chốt chính xác signature và contract lỗi trong PR trước khi cả hai owner code.

Type của contract do **module cung cấp** khai báo. Ví dụ `OrderPaymentService` khai báo `VerifiedPaymentCommand` bất biến trong contract service của order; payment chuyển dữ liệu provider sang command đó. Order không import DTO hoặc enum của payment, nếu không sẽ tạo vòng phụ thuộc dù service chỉ gọi một chiều. Tương tự, inventory chuyển lệnh sang type stock contract do catalog sở hữu.

## Quy tắc transaction xuyên module

Use case điều phối mở transaction; các service ghi tham gia cùng transaction bằng REQUIRED hoặc MANDATORY cho helper không được chạy độc lập. Không REQUIRES_NEW cho stock/ledger/coupon trong checkout. Không gọi HTTP provider khi đang giữ khóa DB. Không truyền entity giữa module dù cùng PersistenceContext.

Phải gọi phương thức transactional qua Spring bean proxy; gọi `this.otherMethod()` không tạo transaction mới chỉ vì có annotation. Xem [Spring transaction annotations](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html).

## Đọc dữ liệu và báo cáo

Mặc định lấy dữ liệu module khác bằng query service/DTO. Chưa cho phép repository tùy ý join entity module khác. Khi báo cáo cần join SQL xuyên bảng, viết proposal về quyền đọc, owner, trường nhạy cảm và truy vấn cụ thể; thống nhất với nhóm nếu mở ngoại lệ. Không thêm một “common repository” toàn hệ thống.
