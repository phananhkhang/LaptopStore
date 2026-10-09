# Module cart — Giỏ hàng

**Owner đề xuất:** C theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). Chưa gán thành viên thật. **Trạng thái:** đã triển khai CRUD backend và contract giỏ cho checkout; order/frontend chưa được triển khai trong phần này.

## Trách nhiệm

Giỏ tạm của từng USER, thêm/xóa/sửa số lượng và cung cấp snapshot checkout. Không tạo đơn, giữ tồn hoặc chốt giá.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | Cart, CartItem |
| Repository | CartRepository, CartItemRepository |
| Service | CartService |
| Controller | CartController |
| Request | AddCartItemRequest, UpdateCartItemRequest |
| Response/contract | CartResponse, CartSnapshot |

Đây là danh sách gợi ý theo use case, không bắt buộc tạo đủ class trước khi có nhu cầu.

DTO dùng class với getter theo chuẩn JavaBean (`getQuantity()`, `getItems()`, ...); request/response HTTP có constructor rỗng và constructor đầy đủ. `CartSnapshot` và `CartSnapshot.Item` là class bất biến; danh sách items được sao chép khi tạo snapshot.

## API đã triển khai

Tất cả endpoint lấy userId từ principal và yêu cầu đăng nhập.

| Method/path | Request | Kết quả |
|---|---|---|
| GET `/api/v1/me/cart` | Không | 200 CartResponse; user chưa có cart nhận giỏ rỗng với id null |
| POST `/api/v1/me/cart/items` | `{ "productVariantId": 7, "quantity": 2 }` | 201 CartResponse; gộp quantity khi đã có variant |
| PATCH `/api/v1/me/cart/items/{cartItemId}` | `{ "quantity": 3 }` | 200 CartResponse; thay quantity |
| DELETE `/api/v1/me/cart/items/{cartItemId}` | Không | 204; item không thuộc giỏ: 404 |
| DELETE `/api/v1/me/cart/items/clear` | Không | 204, kể cả user chưa có giỏ hoặc giỏ đã rỗng |

Mỗi item trả `cartItemId`, `variantId`, tên sản phẩm, SKU, tên cấu hình, giá hiện tại và thành tiền; PATCH/DELETE dùng **cartItemId**, không dùng variantId. `totalItems` là tổng quantity; `totalAmount` là tổng `lineTotal`. Giá này chỉ để hiển thị và phải được order tính lại khi checkout.

Thêm item yêu cầu cả product và variant ACTIVE. Variant đã inactive vẫn được đọc/sửa quantity/xóa trong giỏ; checkout phải xác minh lại khả năng mua và tồn. Cart không thay đổi tồn kho.

## Dữ liệu sản phẩm

Cart hiện chỉ lưu `variantId` và `quantity`. Theo phạm vi hiện tại, cart gọi trực tiếp `ProductVariantRepository` để đọc dữ liệu hiển thị; không ghi dữ liệu catalog. Order/checkout vẫn phải lấy và chốt lại giá khi tạo đơn.

## Contract checkout đã triển khai

- `lockAndGetCheckoutCart(userId)` trả `CartSnapshot(cartId, userId, items)` bất biến; mỗi item chứa cartItemId, variantId và quantity. Giỏ không tồn tại: 404; giỏ rỗng: 400.
- `clearPurchasedItems(userId, cartItemIds)` xác minh toàn bộ ID thuộc giỏ trước khi xóa, chỉ xóa các mục được truyền vào. ID sai/thuộc giỏ khác: 404, không xóa một phần.
- Cả hai dùng `Propagation.MANDATORY`: order phải mở transaction bao trùm snapshot và cleanup. Gọi ngoài transaction bị từ chối. Mọi thao tác ghi khóa hàng cart trước khi tải collection items; không dùng fetch join collection khi khóa.
- CartItem không khai báo UQ ngoài migration V1; khóa cart và gộp variant bảo vệ thao tác ghi qua service. Không sửa migration đã có.

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** carts, cart_items.

**Được gọi:** catalog qua `ProductVariantRepository` để hiển thị cart. Không gọi order.

## Bất biến và ràng buộc

- Mỗi user tối đa một cart vì `carts.user_id` là UQ. ERD không có UQ `(cart_id, variant_id)`; service phải tìm/gộp dòng cùng variant trong cart. Quantity phải lớn hơn 0.
- actorId lấy từ principal; không nhận userId tùy ý trong request.
- Giá hiển thị trong giỏ chỉ là tham khảo; order phải kiểm tra và tính lại khi checkout.
- Mọi thao tác sửa giỏ và checkout khóa cùng hàng cart để tránh xóa mất mục vừa được người dùng thêm.
- clearPurchasedItems chỉ xóa các mục checkout đã chọn, trong transaction của order. Không giữ tồn khi thêm giỏ.

## Luồng triển khai điển hình

Khóa cart thuộc actor → thêm/gộp `variantId` và quantity → trả giỏ tạm thời.

## Bàn giao và kiểm tra

Các ca cần chứng minh: quantity 0/âm; hai lần thêm cùng SKU không tạo dòng trùng; không sửa giỏ người khác; checkout đồng thời cập nhật giỏ. Cập nhật API, migration và test module cùng PR; ghi kết quả thật. Owner phối hợp người dùng contract khi đổi input/output/lỗi.

## Quy tắc các tầng và liên kết kiến trúc

Quy tắc và ràng buộc chi tiết cho từng tầng (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) tuân thủ theo [luật tầng](../../../../../../../docs/architecture/02-structure-and-layers.md) và [luồng nghiệp vụ](../../../../../../../docs/architecture/06-business-flows.md).
