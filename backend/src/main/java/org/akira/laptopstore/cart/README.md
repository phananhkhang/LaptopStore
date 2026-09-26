# Module cart — Giỏ hàng

**Owner đề xuất:** C theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). Chưa gán thành viên thật. **Trạng thái:** skeleton hướng dẫn; các class dưới đây chưa triển khai. Thiết kế chi tiết cần được nhóm chốt khi nhận task.

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

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** carts, cart_items.

**Được gọi:** catalog.service để kiểm tra variant và lấy dữ liệu hiển thị. Không gọi order. Không gọi repository/entity module khác. Xem [allowlist và contract](../../../../../../../docs/architecture/04-dependencies.md).

## Bất biến và ràng buộc

- Mỗi user tối đa một cart vì `carts.user_id` là UQ. ERD không có UQ `(cart_id, variant_id)`; service phải tìm/gộp dòng cùng variant trong cart. Quantity phải lớn hơn 0.
- actorId lấy từ principal; không nhận userId tùy ý trong request.
- Giá hiển thị trong giỏ chỉ là tham khảo; order phải kiểm tra và tính lại khi checkout.
- Mọi thao tác sửa giỏ và checkout khóa cùng hàng cart để tránh xóa mất mục vừa được người dùng thêm.
- clearPurchasedItems chỉ xóa các mục checkout đã chọn, trong transaction của order. Không giữ tồn khi thêm giỏ.

## Luồng triển khai điển hình

Khóa cart thuộc actor → kiểm tra variant qua catalog → thêm/gộp quantity → trả giỏ.

## Bàn giao và kiểm tra

Các ca cần chứng minh: quantity 0/âm; hai lần thêm cùng SKU không tạo dòng trùng; không sửa giỏ người khác; checkout đồng thời cập nhật giỏ. Cập nhật API, migration và test module cùng PR; ghi kết quả thật. Owner phối hợp người dùng contract khi đổi input/output/lỗi.

## Quy tắc các tầng và liên kết kiến trúc

Quy tắc và ràng buộc chi tiết cho từng tầng (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) tuân thủ theo [luật tầng](../../../../../../../docs/architecture/02-structure-and-layers.md) và [luồng nghiệp vụ](../../../../../../../docs/architecture/06-business-flows.md).
