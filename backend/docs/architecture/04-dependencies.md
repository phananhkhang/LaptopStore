# Phụ thuộc và hợp đồng giữa các module

## Allowlist

Mũi tên A → B nghĩa là service A gọi contract service/DTO của B. “Không” nghĩa là không gọi module nghiệp vụ khác. Ngoài danh sách dưới đây bị cấm cho đến khi cập nhật thiết kế; hạ tầng chung được mô tả riêng.

| Module gọi | Module được gọi | Mục đích |
|---|---|---|
| auth | user | Tạo tài khoản, lấy credential/profile |
| user | Không | Nguồn người dùng và địa chỉ |
| catalog | Không | Nguồn sản phẩm, variant, giá, thông số |
| cart | catalog | Xác minh variant, dữ liệu hiển thị |
| order | user, cart, catalog, inventory, promotion | Điều phối checkout/hủy |
| payment | order | Đọc payable order, cập nhật kết quả đã xác minh |
| promotion | Không | Nhận các giá trị server truyền vào |
| inventory | catalog | Khóa variant, đọc và cập nhật `product_variants.quantity` qua contract; không sửa metadata/giá |
| procurement | catalog, inventory | Kiểm tra SKU/variant và nhận hàng |
| review | order, catalog | Kiểm tra đã mua/giao và thông tin public |

Không có cạnh `catalog → inventory`, `order → payment`, `cart → order` hoặc `catalog → review`. ERD đặt tồn tại `product_variants.quantity`; catalog cung cấp contract nội bộ có phạm vi hẹp để inventory khóa và cập nhật đúng cột này.

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
| catalog | `getSellableVariants(ids)` | ids → VariantSnapshot[] | Dùng cho đọc/giỏ; kiểm tra active, lấy giá mới, không trả entity |
| catalog | `lockAndGetCheckoutVariants(ids)` | ids → CheckoutVariantSnapshot[] | Khóa variant theo id tăng dần trong transaction checkout; chốt giá/trạng thái/SKU snapshot |
| catalog | `getVariantReferences(ids)` | ids → VariantReference[] | Read-only cho inventory/procurement khi cần xác minh ID/SKU |
| catalog | `changeVariantQuantity(...)` | variantId, delta → before/after | Chỉ inventory gọi; khóa variant, không âm, không sửa metadata/giá |
| inventory | `deductForOrder(...)`, `restoreForOrder(...)` | orderId, lines, operationKey → kết quả | Khóa variant tăng dần; cập nhật quantity và ledger atomic; mỗi movement có operation key UQ |
| inventory | `receivePurchase(...)` | purchaseId, lines, operationKey | Cùng transaction nhận phiếu; tăng stock và ledger atomic |
| inventory | `adjustStock(...)` | variantId, delta, reason, actorId, operationKey | Không âm; bắt buộc reason cho adjustment |
| promotion | `lockAndEvaluateForCheckout(...)` | coupon, userId, subtotal → CouponEvaluation | Khóa coupon; không giữ lượt ngoài transaction checkout |
| promotion | `reserveForOrder(...)`, `releaseForOrder(...)` | coupon, userId, orderId, subtotal → CouponEvaluation | Khóa và ghi usage; không tự query order |
| order | `getPayableOrder(actorId, orderId)` | ID → OrderPaymentSnapshot | Ownership, total và trạng thái order; payment tự quản lý method/status |
| order | `recordVerifiedPayment(result)` | kết quả nội bộ từ payment → outcome | Khóa order, kiểm tra trạng thái; trạng thái thanh toán vẫn nằm ở `payments` |
| order | `getDeliveredItem(userId, orderItemId)` | ID → PurchasedItemView | Chỉ đúng chủ đơn và DELIVERED |

Lỗi chéo dùng exception cơ sở/mã lỗi đã công bố, ví dụ `INSUFFICIENT_STOCK`, `INVALID_ORDER_STATE`, `COUPON_NOT_APPLICABLE`. Caller không bắt mọi Exception rồi trả thành công. Chốt chính xác signature và contract lỗi trong PR trước khi cả hai owner code.

Type của contract do **module cung cấp** khai báo. Ví dụ `OrderPaymentService` khai báo `VerifiedPaymentCommand` bất biến trong contract service của order; payment chuyển dữ liệu provider sang command đó. Order không import DTO hoặc enum của payment, nếu không sẽ tạo vòng phụ thuộc dù service chỉ gọi một chiều.

## Quy tắc entity/JPA xuyên module

- Quan hệ JPA (`@OneToMany`, `@ManyToOne`, ...) chỉ dùng bên trong cùng module.
- Sang module khác chỉ giữ ID (`Long userId`, `Long variantId`, ...), dù DB vẫn có foreign key.
- Không truyền JPA entity qua contract module; chỉ truyền record/DTO bất biến.
- Không module nào import `..repository..` hoặc `..entity..` của module khác.

Ví dụ `StockMovement` giữ `variantId`; không có `@ManyToOne ProductVariant`. `OrderItem` giữ `variantId` và snapshot tên/SKU/giá; không có quan hệ JPA sang catalog.

## Quy tắc transaction xuyên module

Use case điều phối mở transaction; các service ghi tham gia cùng transaction bằng REQUIRED hoặc MANDATORY cho helper không được chạy độc lập. Không REQUIRES_NEW cho stock/ledger/coupon trong checkout. Không gọi HTTP provider khi đang giữ khóa DB. Không truyền entity giữa module dù cùng PersistenceContext.

Checkout khóa theo thứ tự ổn định: cart → catalog variants (ID tăng dần) → coupon nếu chiến lược yêu cầu. Inventory cập nhật quantity trên chính các variant đã khóa và ghi movement trong cùng transaction. Nếu nhóm đổi thứ tự khóa, phải cập nhật tất cả luồng ghi liên quan để tránh deadlock.

Phải gọi phương thức transactional qua Spring bean proxy; gọi `this.otherMethod()` không tạo transaction mới chỉ vì có annotation.

## Đọc dữ liệu và báo cáo

Mặc định lấy dữ liệu module khác bằng query service/DTO. Chưa cho phép repository tùy ý join entity module khác. Khi báo cáo cần join SQL xuyên bảng, viết proposal về quyền đọc, owner, trường nhạy cảm và truy vấn cụ thể; thống nhất với nhóm nếu mở ngoại lệ. Không thêm một “common repository” toàn hệ thống.
